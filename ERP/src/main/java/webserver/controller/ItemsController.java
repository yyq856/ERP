package webserver.controller;

import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.ItemsTabQueryRequest;
import webserver.pojo.ValidateItemsResponse;
import webserver.service.ItemsService;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/app/items")
public class ItemsController {

    private final ItemsService itemsService;

    public ItemsController(ItemsService itemsService) {
        this.itemsService = itemsService;
    }

    @PostMapping("/items-tab-query")
    public Response<ValidateItemsResponse> itemsTabQuery(@RequestBody List<ItemsTabQueryRequest.ItemInput> items) {
        return itemsService.itemsTabQuery(items);
    }
}

