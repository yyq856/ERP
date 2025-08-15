#!/bin/bash

# 完整的outbound delivery测试脚本
# 包括：启动后端、设置测试数据、运行API测试、清理

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
BACKEND_DIR="."
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="erp_db"
DB_USER="root"
DB_PASS="123456"
BACKEND_PORT="8080"

echo -e "${BLUE}=== Outbound Delivery 完整测试流程 ===${NC}"
echo

# 检查必要的工具
check_tools() {
    echo "检查必要工具..."
    
    if ! command -v java &> /dev/null; then
        echo -e "${RED}错误: Java未安装${NC}"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null && ! [ -f "./mvnw" ]; then
        echo -e "${RED}错误: Maven未安装且无mvnw${NC}"
        exit 1
    fi
    
    if ! command -v mysql &> /dev/null; then
        echo -e "${YELLOW}警告: MySQL客户端未安装，跳过数据库操作${NC}"
        SKIP_DB=true
    fi
    
    if ! command -v curl &> /dev/null; then
        echo -e "${RED}错误: curl未安装${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ 工具检查完成${NC}"
}

# 设置测试数据
setup_test_data() {
    if [ "$SKIP_DB" = true ]; then
        echo -e "${YELLOW}跳过数据库设置${NC}"
        return
    fi
    
    echo "设置测试数据..."
    
    # 检查数据库连接
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "USE $DB_NAME;" 2>/dev/null; then
        echo -e "${RED}错误: 无法连接到数据库${NC}"
        echo "请检查数据库配置: $DB_USER@$DB_HOST:$DB_PORT/$DB_NAME"
        exit 1
    fi
    
    # 执行表结构更新
    echo "更新表结构..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < src/main/resources/sql/build.sql
    
    # 设置测试数据
    echo "插入测试数据..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < setup-test-data.sql
    
    echo -e "${GREEN}✓ 测试数据设置完成${NC}"
}

# 启动后端服务
start_backend() {
    echo "启动后端服务..."
    
    # 检查端口是否被占用
    if lsof -Pi :$BACKEND_PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}端口 $BACKEND_PORT 已被占用，尝试停止现有服务...${NC}"
        pkill -f "spring-boot" || true
        sleep 3
    fi
    
    # 编译项目
    echo "编译项目..."
    if [ -f "./mvnw" ]; then
        ./mvnw clean compile -DskipTests
    else
        mvn clean compile -DskipTests
    fi
    
    # 启动服务（后台运行）
    echo "启动Spring Boot应用..."
    if [ -f "./mvnw" ]; then
        nohup ./mvnw spring-boot:run > backend.log 2>&1 &
    else
        nohup mvn spring-boot:run > backend.log 2>&1 &
    fi
    
    BACKEND_PID=$!
    echo "后端服务PID: $BACKEND_PID"
    
    # 等待服务启动
    echo "等待后端服务启动..."
    for i in {1..60}; do
        if curl -s "http://localhost:$BACKEND_PORT/actuator/health" > /dev/null 2>&1 || \
           curl -s "http://localhost:$BACKEND_PORT/api/app/outbound-delivery/get-deliveries-summary" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ 后端服务启动成功${NC}"
            return 0
        fi
        echo "等待中... ($i/60)"
        sleep 2
    done
    
    echo -e "${RED}错误: 后端服务启动超时${NC}"
    echo "查看日志:"
    tail -20 backend.log
    return 1
}

# 运行API测试
run_api_tests() {
    echo "运行API测试..."
    
    # 给脚本执行权限
    chmod +x test-outbound-api.sh
    
    # 运行测试
    if ./test-outbound-api.sh; then
        echo -e "${GREEN}✓ API测试完成${NC}"
        return 0
    else
        echo -e "${RED}✗ API测试失败${NC}"
        return 1
    fi
}

# 清理资源
cleanup() {
    echo "清理资源..."
    
    # 停止后端服务
    if [ ! -z "$BACKEND_PID" ]; then
        echo "停止后端服务 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID 2>/dev/null || true
        sleep 2
        kill -9 $BACKEND_PID 2>/dev/null || true
    fi
    
    # 清理其他可能的Java进程
    pkill -f "spring-boot" 2>/dev/null || true
    
    echo -e "${GREEN}✓ 清理完成${NC}"
}

# 主函数
main() {
    # 设置清理陷阱
    trap cleanup EXIT
    
    # 进入后端目录
    cd "$BACKEND_DIR"
    
    # 执行测试流程
    check_tools
    setup_test_data
    start_backend
    
    # 等待一下确保服务完全启动
    sleep 5
    
    run_api_tests
    
    echo
    echo -e "${BLUE}=== 测试完成 ===${NC}"
    echo "后端日志文件: backend.log"
    echo "如需查看详细日志: tail -f backend.log"
}

# 处理命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-db)
            SKIP_DB=true
            shift
            ;;
        --db-host)
            DB_HOST="$2"
            shift 2
            ;;
        --db-port)
            DB_PORT="$2"
            shift 2
            ;;
        --db-name)
            DB_NAME="$2"
            shift 2
            ;;
        --db-user)
            DB_USER="$2"
            shift 2
            ;;
        --db-pass)
            DB_PASS="$2"
            shift 2
            ;;
        --backend-port)
            BACKEND_PORT="$2"
            shift 2
            ;;
        --help)
            echo "用法: $0 [选项]"
            echo "选项:"
            echo "  --skip-db          跳过数据库操作"
            echo "  --db-host HOST     数据库主机 (默认: localhost)"
            echo "  --db-port PORT     数据库端口 (默认: 3306)"
            echo "  --db-name NAME     数据库名称 (默认: erp_db)"
            echo "  --db-user USER     数据库用户 (默认: root)"
            echo "  --db-pass PASS     数据库密码 (默认: 123456)"
            echo "  --backend-port PORT 后端端口 (默认: 8080)"
            echo "  --help             显示帮助"
            exit 0
            ;;
        *)
            echo "未知选项: $1"
            echo "使用 --help 查看帮助"
            exit 1
            ;;
    esac
done

# 运行主函数
main
