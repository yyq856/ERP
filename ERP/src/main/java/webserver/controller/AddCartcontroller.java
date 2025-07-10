package webserver.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import webserver.pojo.Fav;
import webserver.pojo.Result;
import webserver.service.AddCartService;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class AddCartcontroller {

    @Autowired
    private AddCartService addCartService;

    @PostMapping("/fav/addcart")
    public Result addFav(@RequestBody Fav fav) {
        log.info("addcart");
        addCartService.addcart(fav);
        return Result.success();
    }
}
