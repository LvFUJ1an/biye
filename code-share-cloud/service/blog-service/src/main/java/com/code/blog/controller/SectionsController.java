package com.code.blog.controller;

import com.alibaba.fastjson.JSONObject;
import com.app.code.common.BaseResponse;
import com.app.code.model.dto.SectionsDTO;
import com.app.code.model.po.Sections;
import com.app.code.model.po.User;
import com.app.code.model.vo.SectionsVO;
import com.app.code.util.UserUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.blog.service.SectionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 板块管理接口
 * @author lfj
 * @createDate 2025/4/26
 */
@RestController
@RequestMapping("/sections")
public class SectionsController {

    @Autowired
    private SectionsService sectionsService;

    /**
     * 创建板块
     * @param sections 板块信息
     * @return 创建结果
     */
    @PostMapping
    public BaseResponse<Sections> createSection(@RequestBody Sections sections, HttpServletRequest request) {
        // 设置创建时间
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        sections.setCreateTime(new Date());
        sections.setAdminId(user.getId());
        boolean result = sectionsService.save(sections);
        if (result) {
            return BaseResponse.success(sections);
        } else {
            return BaseResponse.error(500, "创建板块失败");
        }
    }

    /**
     * 更新板块
     * @param sections 板块信息
     * @return 更新结果
     */
    @PostMapping("/modify")
    public BaseResponse<Boolean> updateSection(@RequestBody Sections sections) {
        boolean result = sectionsService.updateById(sections);
        if (result) {
            return BaseResponse.success(true);
        } else {
            return BaseResponse.error(500, "更新板块失败");
        }
    }

    /**
     * 删除板块
     * @param id 板块ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteSection(@PathVariable Integer id) {
        boolean result = sectionsService.removeById(id);
        if (result) {
            return BaseResponse.success(true);
        } else {
            return BaseResponse.error(500, "删除板块失败");
        }
    }

    /**
     * 根据ID获取板块
     * @param id 板块ID
     * @return 板块信息
     */
    @GetMapping("/{id}")
    public BaseResponse<Sections> getSection(@PathVariable Integer id) {
        Sections sections = sectionsService.getById(id);
        if (sections != null) {
            return BaseResponse.success(sections);
        } else {
            return BaseResponse.error(404, "板块不存在");
        }
    }

    /**
     * 多条件分页查询板块
     * @param sectionsDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    public BaseResponse<Page<Sections>> pageQuery(@RequestBody SectionsDTO sectionsDTO) {
        Page<Sections> page = sectionsService.pageQuery(sectionsDTO);
        return BaseResponse.success(page);
    }

    /**
     * 门户首页展示板块
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<SectionsVO>> list() {
        List<Sections> list = sectionsService.list();
        List<SectionsVO> listVO = new ArrayList<>();
        list.forEach(item ->{
            SectionsVO sectionsVO = new SectionsVO();
            sectionsVO.setId(item.getId());
            sectionsVO.setName(item.getName());
            sectionsVO.setDescription(item.getDescription());
            //todo 需要实际计算板块下的内容数量
            sectionsVO.setCount("1.5k");
            sectionsVO.setAdminId(item.getAdminId());
            sectionsVO.setCreateTime(item.getCreateTime());
            listVO.add(sectionsVO);
        });
        return BaseResponse.success(listVO);
    }
}
