package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.Material;

@Mapper
public interface MaterialMapper {
    Material findById(@Param("matId") Long matId);
}

