package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.*;
import webserver.service.BpRelationshipService;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class BpRelationshipController {

    @Autowired
    private BpRelationshipService bpRelationshipService;

    /**
     * 业务伙伴关系注册接口
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/api/app/bp-relationship/register")
    public BpRelationshipResponse register(@RequestBody BpRelationshipRegisterRequest request) {
        log.info("业务伙伴关系注册请求: {}", 
                request.getRelation() != null ? request.getRelation().getRelationShipCategory() : "null");
        return bpRelationshipService.register(request);
    }

    /**
     * 业务伙伴关系查询接口
     * @param request 查询请求
     * @return 查询结果
     */
    @PostMapping("/api/app/bp-relationship/get")
    public BpRelationshipResponse get(@RequestBody BpRelationshipGetRequest request) {
        log.info("业务伙伴关系查询请求: {}", request.getRelationshipId());
        return bpRelationshipService.get(request);
    }

    /**
     * 业务伙伴关系编辑接口
     * @param request 编辑请求
     * @return 编辑结果
     */
    @PostMapping("/api/app/bp-relationship/edit")
    public BpRelationshipResponse edit(@RequestBody BpRelationshipEditRequest request) {
        log.info("业务伙伴关系编辑请求");
        return bpRelationshipService.edit(request);
    }
}
