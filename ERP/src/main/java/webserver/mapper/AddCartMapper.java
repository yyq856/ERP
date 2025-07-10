package webserver.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AddCartMapper {
    @Insert("INSERT INTO shopping_cart_items (cartid, productid,number) " +
            "SELECT c.cartid, #{productId},1 FROM shopping_cart c WHERE c.userid = #{userId}")
    int addcart(@Param("userId") String userId, @Param("productId") String productId);
}
