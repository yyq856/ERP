#!/bin/bash

# 出库交货单API测试脚本
# 用于测试所有6个outbound delivery接口

BASE_URL="http://localhost:8080/api/app/outbound-delivery"
CONTENT_TYPE="Content-Type: application/json"

echo "=== 出库交货单API测试开始 ==="
echo "Base URL: $BASE_URL"
echo

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 测试函数
test_api() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_success="$5"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "${BLUE}测试 $TOTAL_TESTS: $test_name${NC}"
    echo "请求: $method $endpoint"
    
    if [ "$method" = "POST" ]; then
        echo "数据: $data"
        response=$(curl -s -X POST "$BASE_URL$endpoint" \
            -H "$CONTENT_TYPE" \
            -d "$data" \
            -w "\nHTTP_CODE:%{http_code}")
    else
        response=$(curl -s -X GET "$BASE_URL$endpoint" \
            -w "\nHTTP_CODE:%{http_code}")
    fi
    
    # 提取HTTP状态码
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    response_body=$(echo "$response" | sed '/HTTP_CODE:/d')
    
    echo "HTTP状态码: $http_code"
    echo "响应: $response_body"
    
    # 检查是否包含success字段
    if echo "$response_body" | grep -q '"success"'; then
        success_value=$(echo "$response_body" | grep -o '"success":[^,}]*' | cut -d: -f2 | tr -d ' ')
        echo "Success字段: $success_value"
        
        if [ "$success_value" = "$expected_success" ] && [ "$http_code" = "200" ]; then
            echo -e "${GREEN}✓ 测试通过${NC}"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        else
            echo -e "${RED}✗ 测试失败 (期望success=$expected_success, HTTP=200)${NC}"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        if [ "$http_code" = "200" ]; then
            echo -e "${YELLOW}? 响应格式异常但HTTP成功${NC}"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        else
            echo -e "${RED}✗ 测试失败 (HTTP=$http_code)${NC}"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    fi
    
    echo "----------------------------------------"
    echo
}

# 等待后端启动
echo "检查后端服务状态..."
for i in {1..30}; do
    if curl -s "$BASE_URL/../health" > /dev/null 2>&1 || curl -s "http://localhost:8080/health" > /dev/null 2>&1; then
        echo -e "${GREEN}后端服务已启动${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}后端服务启动超时，继续测试...${NC}"
    fi
    echo "等待后端启动... ($i/30)"
    sleep 2
done

echo

# 1. 测试创建出库交货单
test_api "创建出库交货单" "POST" "/create-from-orders" \
'{
    "salesOrderIds": ["6001", "6002"],
    "selectedOrders": [
        {"id": "6001"},
        {"id": "6002"}
    ]
}' "true"

# 2. 测试获取汇总列表
test_api "获取汇总列表-全部" "POST" "/get-deliveries-summary" \
'{
    "overallStatus": null
}' "true"

test_api "获取汇总列表-进行中" "POST" "/get-deliveries-summary" \
'{
    "overallStatus": "IN_PROGRESS"
}' "true"

# 3. 测试获取详情
test_api "获取交货单详情" "POST" "/get-detail" \
'{
    "deliveryId": "1"
}' "true"

# 4. 测试物品验证更新
test_api "物品验证更新" "POST" "/items-tab-query" \
'[
    {
        "dlvId": 1,
        "itemNo": 10,
        "item": "10",
        "material": "MAT-001",
        "pickingQuantity": 100,
        "pickingStatus": "Completed",
        "confirmationStatus": "Not Confirmed",
        "itemType": "Standard",
        "conversionRate": 1.000,
        "storageLocation": "0001",
        "storageBin": "A-01-01"
    }
]' "true"

# 5. 测试根据ID过账
test_api "根据ID批量过账" "POST" "/post-gis-by-id" \
'{
    "deliveryIds": ["1"]
}' "true"

# 6. 测试物品过账
test_api "物品过账" "POST" "/post-gis" \
'[
    {
        "items": [
            {
                "dlvId": 1,
                "itemNo": 10,
                "item": "10",
                "material": "MAT-001",
                "pickingQuantity": 100,
                "confirmationStatus": "Confirmed",
                "storageLocation": "0001",
                "storageBin": "A-01-01"
            }
        ]
    }
]' "true"

# 错误情况测试
echo -e "${YELLOW}=== 错误情况测试 ===${NC}"

# 测试不存在的交货单
test_api "获取不存在的交货单" "POST" "/get-detail" \
'{
    "deliveryId": "99999"
}' "false"

# 测试空的创建请求
test_api "空的创建请求" "POST" "/create-from-orders" \
'{
    "salesOrderIds": []
}' "true"

# 测试无效的物品数据
test_api "无效物品数据验证" "POST" "/items-tab-query" \
'[
    {
        "dlvId": null,
        "itemNo": null,
        "pickingQuantity": -1
    }
]' "true"

# 输出测试结果统计
echo
echo "=== 测试结果统计 ==="
echo -e "总测试数: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "通过: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}❌ 有 $FAILED_TESTS 个测试失败${NC}"
    exit 1
fi
