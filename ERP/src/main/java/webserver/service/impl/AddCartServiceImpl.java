package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.mapper.AddCartMapper;
import webserver.pojo.Fav;
import webserver.service.AddCartService;

@Service
public class AddCartServiceImpl implements AddCartService {

    @Autowired
    private AddCartMapper addCartMapper;



    @Override
    public void addcart(Fav fav) {
        addCartMapper.addcart(fav.getUserid(),fav.getProductid());
    }
}

