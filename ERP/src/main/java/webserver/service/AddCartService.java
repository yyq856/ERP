package webserver.service;
import org.springframework.stereotype.Service;
import webserver.pojo.Fav;

@Service
public interface AddCartService {
    void addcart(Fav fav);
}
