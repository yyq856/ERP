package webserver.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import webserver.pojo.User;

@Mapper
public interface UserMapper {
    
    /**
     * 根据用户名查询用户 (将username作为id查询)
     * @param username 用户名(实际查询id字段)
     * @return 用户信息
     */
    @Select("SELECT id, id as username, password, null as email, null as createTime, null as updateTime FROM erp_account WHERE id = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 检查用户名是否存在 (检查id是否存在)
     * @param username 用户名(实际检查id字段)
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM erp_account WHERE id = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * 插入新用户 (将username作为id插入)
     * @param username 用户名(作为id存储)
     * @param password 密码
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_account (id, password) VALUES (#{username}, #{password})")
    int insertUser(@Param("username") String username, @Param("password") String password);
}
