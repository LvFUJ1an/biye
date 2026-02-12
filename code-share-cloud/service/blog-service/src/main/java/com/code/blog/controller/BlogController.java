package com.code.blog.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.app.code.common.BaseResponse;
import com.app.code.model.po.*;
import com.app.code.model.request.ModifyBlogRequest;
import com.app.code.model.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.blog.client.UserFeignClient;
import com.code.blog.service.*;
import com.code.blog.ws.WebSocketServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 博客管理接口
 * @author lfj
 * @createDate 2025/4/27
 */
@RestController
@RequestMapping("/blog")
public class BlogController {
    
    @Resource
    private BlogsService blogsService;

    @Resource
    private CommentsService commentsService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private SectionsService sectionsService;

    @Resource
    private LikeService likeService;

    @Resource
    private FollowService followService;

    @Resource
    private VideosService videosService;;

    @Resource
    private QuestionsService questionsService;

    @Resource
    private VotesService votesService;
    
    /**
     * 创建博客
     * @param blogs 博客信息
     * @return 创建结果
     */
    @PostMapping("/create")
    public BaseResponse<Blogs> createBlog(@RequestBody Blogs blogs, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        if  (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);

        blogs.setVersion(0);
        blogs.setDataId(IdUtil.simpleUUID());
        blogs.setAuthorId(user.getId());
        // 设置创建时间和更新时间
        Date now = new Date();
        blogs.setCreateTime(now);
        blogs.setUpdateTime(now);
        // 设置版本号
        boolean result = blogsService.save(blogs);
        if (result) {
            return BaseResponse.success(blogs);
        } else {
            return BaseResponse.error(500, "创建博客失败");
        }
    }
    
    /**
     * 更新博客
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateBlog(@RequestBody ModifyBlogRequest modifyBlogRequest,HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        if  (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User loginUser = JSONObject.parseObject(userInfo, User.class);
        Integer id = modifyBlogRequest.getId();
        if (id == null) {
            return BaseResponse.error(400, "博客ID不能为空");
        }
        Blogs oldBlog = blogsService.getById(id);
        // 设置更新时间
        oldBlog.setUpdateTime(new Date());
        // 更新版本号

        LambdaQueryWrapper<Blogs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blogs::getDataId, oldBlog.getDataId());
        String subSql = ""
                + "SELECT a.id "
                + "FROM blogs a "
                + "INNER JOIN ("
                + "    SELECT data_id, MAX(version) AS version "
                + "    FROM blogs "
                + "    GROUP BY data_id"
                + ") m ON a.data_id = m.data_id AND a.version = m.version";
        queryWrapper.inSql(Blogs::getId, subSql);
        Blogs blogs = blogsService.getOne(queryWrapper);
        Integer version = blogs.getVersion();

        oldBlog.setVersion(version + 1);
        oldBlog.setContent(modifyBlogRequest.getContent());
        oldBlog.setTitle(modifyBlogRequest.getTitle());
        oldBlog.setSectionId(modifyBlogRequest.getSectionId());
        oldBlog.setStatus(modifyBlogRequest.getStatus());

        if (!Objects.equals(loginUser.getId(), oldBlog.getAuthorId())){
            return BaseResponse.error(403, "无权限修改该博客");
        }
        oldBlog.setAuthorId(loginUser.getId());
        oldBlog.setId(null);
        
        boolean result = blogsService.save(oldBlog);
        if (result) {
            return BaseResponse.success(oldBlog.getId());
        } else {
            return BaseResponse.error(500, "更新博客失败");
        }
    }
    
    /**
     * 删除博客
     * @param id 博客ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteBlog(@PathVariable Integer id) {
        LambdaQueryWrapper<Blogs> queryWrapper = new LambdaQueryWrapper<>();
        Blogs blogs = blogsService.getById(id);
        if (blogs == null) {
            return BaseResponse.error(404, "博客不存在");
        }
        queryWrapper.eq(Blogs::getDataId, blogs.getDataId());
        boolean result = blogsService.remove(queryWrapper);
        if (result) {
            return BaseResponse.success(true);
        } else {
            return BaseResponse.error(500, "删除博客失败");
        }
    }
    
    /**
     * 根据ID获取博客
     * @param id 博客ID
     * @return 博客信息
     */
    @GetMapping("/{id}")
    public BaseResponse<Blogs> getBlog(@PathVariable Integer id) {
        Blogs blogs = blogsService.getById(id);
        if (blogs != null) {
            return BaseResponse.success(blogs);
        } else {
            return BaseResponse.error(404, "博客不存在");
        }
    }
    
    /**
     * 分页查询博客
     * @param current 当前页
     * @param size 每页大小
     * @param sectionId 板块ID（可选）
     * @param authorId 作者ID（可选）
     * @param status 状态（可选）
     * @param sortBy 排序方式：latest-最新（默认），hottest-最热门
     * @return 分页结果
     */
    @GetMapping("/page")
    public BaseResponse<List<BlogVO>> pageBlog(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer sectionId,
            @RequestParam(required = false) Integer authorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sortBy) {
        
        // 创建分页对象
        Page<Blogs> page = new Page<>(current, size);
        
        // 创建查询条件
        LambdaQueryWrapper<Blogs> queryWrapper = new LambdaQueryWrapper<>();
        String subSql = ""
                + "SELECT a.id "
                + "FROM blogs a "
                + "INNER JOIN ("
                + "    SELECT data_id, MAX(version) AS version "
                + "    FROM blogs "
                + "    GROUP BY data_id"
                + ") m ON a.data_id = m.data_id AND a.version = m.version";

        queryWrapper.inSql(Blogs::getId, subSql);
        // 添加查询条件
        if (sectionId != null) {
            queryWrapper.eq(Blogs::getSectionId, sectionId);
        }
        
        if (authorId != null) {
            queryWrapper.eq(Blogs::getAuthorId, authorId);
        }
        
        if (status != null) {
            queryWrapper.eq(Blogs::getStatus, status);
        }
        
        // 根据排序类型设置排序条件
        if ("hottest".equals(sortBy)) {
            // 按点赞数降序排序（最热门）
            queryWrapper.orderByDesc(Blogs::getLikesCount);
            // 如果点赞数相同，再按创建时间降序
            queryWrapper.orderByDesc(Blogs::getCreateTime);
        } else {
            // 默认按创建时间降序排序（最新）
            queryWrapper.orderByDesc(Blogs::getCreateTime);
        }

        if (keyword != null){
            queryWrapper.like(Blogs::getTitle, keyword);
            queryWrapper.or().like(Blogs::getContent, keyword);
        }

        // 执行分页查询
        Page<Blogs> result = blogsService.page(page, queryWrapper);
        //获取每个博客的评论数和点赞数
        List<Blogs> blogsList = result.getRecords();

        ArrayList<BlogVO> blogVOS = new ArrayList<>();
        for (Blogs blog : blogsList) {
            BlogVO blogVO = new BlogVO();
            LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comments::getTargetId, blog.getId());
            List<Comments> list = commentsService.list(wrapper);
            Sections sections = sectionsService.getById(blog.getSectionId());
            blogVO.setSectionName(sections.getName());
            Integer likesCount = blog.getLikesCount();
            blog.setLikesCount(likesCount);
            BeanUtils.copyProperties(blog, blogVO);
            blogVO.setCommentCount(list.size());
            blogVO.setLikesCount(likesCount);
            User user = userFeignClient.getUserById(blog.getAuthorId()).getData();
            blogVO.setAuthorName(user.getUsername());
            blogVO.setAuthorAvatar(user.getAvatarUrl());
            blogVOS.add(blogVO);
        }


        return BaseResponse.success(blogVOS);
    }
    
    /**
     * 获取博客详情，包含评论层级结构
     * @param id 博客ID
     * @return 博客详细信息
     */
    @GetMapping("/detail/{id}")
    public BaseResponse<BlogDetailVO> getBlogDetail(@PathVariable Integer id,HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        if  (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User loginUser = JSONObject.parseObject(userInfo, User.class);
        // 1. 获取博客基本信息
        Blogs blog = blogsService.getById(id);
        if (blog == null) {
            return BaseResponse.error(404, "博客不存在");
        }
        Integer viewCount = blog.getViewCount();
        blog.setViewCount(viewCount + 1);
        blogsService.updateById(blog);

        // 2. 创建博客详情VO
        BlogDetailVO blogDetailVO = new BlogDetailVO();
        BeanUtils.copyProperties(blog, blogDetailVO);

        // 3. 获取作者信息
        User author = userFeignClient.getUserById(blog.getAuthorId()).getData();
        blogDetailVO.setAuthorName(author.getUsername());
        blogDetailVO.setAuthorAvatar(author.getAvatarUrl());

        LambdaQueryWrapper<Follow> followWrapper = new LambdaQueryWrapper<>();
        followWrapper.eq(Follow::getUserId,author.getId());
        Follow follow = followService.getOne(followWrapper);
        if (follow != null){
            List<Integer> followIdList = JSONObject.parseArray(follow.getFollowIdList(), Integer.class);
            if (followIdList.contains(loginUser.getId())){
                blogDetailVO.setIsFollow(true);
            }else {
                blogDetailVO.setIsFollow(false);
            }
        }else {
            blogDetailVO.setIsFollow(false);
        }



        // 4. 查询该博客的所有评论
        LambdaQueryWrapper<Comments> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comments::getTargetId, id);
        commentWrapper.orderByDesc(Comments::getCreateTime); // 按时间降序
        List<Comments> allComments = commentsService.list(commentWrapper);

        // 5. 获取评论数和点赞数
        blogDetailVO.setCommentCount(allComments.size());
        blogDetailVO.setLikesCount(blog.getLikesCount());
        blogDetailVO.setSectionName(sectionsService.getById(blog.getSectionId()).getName());
        // 没有评论，直接返回
        if (allComments.isEmpty()) {
            return BaseResponse.success(blogDetailVO);
        }

        // 6. 构建评论层级结构
        // 6.1. 获取所有评论者的ID
        List<Integer> userIds = allComments.stream()
                .map(Comments::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 6.2. 批量获取用户信息（这里需要根据项目实际情况调整，可能需要单个获取）
        Map<Integer, User> userMap = new HashMap<>();
        for (Integer userId : userIds) {
            BaseResponse<User> userResponse = userFeignClient.getUserById(userId);
            if (userResponse != null && userResponse.getData() != null) {
                userMap.put(userId, userResponse.getData());
            }
        }

        // 6.3. 将Comments转换为CommentVO
        List<CommentVO> commentVOList = new ArrayList<>();
        Map<Integer, CommentVO> commentMap = new HashMap<>();

        // 第一次遍历：创建所有CommentVO对象
        for (Comments comment : allComments) {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);

            // 添加用户信息
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                commentVO.setUsername(user.getUsername());
                commentVO.setAvatarUrl(user.getAvatarUrl());
            }

            commentMap.put(comment.getId(), commentVO);
        }

        // 第二次遍历：构建评论树
        for (Comments comment : allComments) {
            CommentVO commentVO = commentMap.get(comment.getId());

            if (comment.getParentId() == 0) {
                // 顶级评论直接添加到结果列表
                commentVOList.add(commentVO);
            } else {
                // 子评论添加到父评论的children列表
                CommentVO parentComment = commentMap.get(comment.getParentId());
                if (parentComment != null) {
                    parentComment.getChildren().add(commentVO);
                }
            }
        }

        // 7. 将顶级评论设置到BlogDetailVO
        blogDetailVO.setComments(commentVOList);

        //8. 获取点赞数量
        LambdaQueryWrapper<Likes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Likes::getResourceId, id);
        List<Likes> list = likeService.list(queryWrapper);
        blogDetailVO.setLikesCount(list.size());
        if (loginUser !=null){
            for (Likes like : list){
                if (like.getUserId().equals(loginUser.getId())){
                    blogDetailVO.setIsLiked("true");
                }
            }
        }

        return BaseResponse.success(blogDetailVO);
    }


    /**
     * 点赞博客
     */
    @PostMapping("/like/{id}")
    public BaseResponse<Boolean> likeBlog(@PathVariable Integer id,HttpServletRequest request) {
        // 1. 获取当前登录用户
        String userInfo = request.getHeader("X-User-Info");
        if (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);

        // 2. 验证博客是否存在
        Blogs blog = blogsService.getById(id);
        User author = userFeignClient.getUserById(blog.getAuthorId()).getData();
        if (blog == null) {
            return BaseResponse.error(404, "博客不存在");
        }

        // 3. 验证用户是否已经点赞
        LambdaQueryWrapper<Likes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Likes::getUserId, user.getId());
        queryWrapper.eq(Likes::getResourceId, id);
        queryWrapper.eq(Likes::getType, 0);
        if (likeService.getOne(queryWrapper) != null) {
            return BaseResponse.error(400, "您已经点赞过该博客");
        }
        Likes likes = new Likes();
        likes.setUserId(user.getId());
        likes.setResourceId(id);
        likes.setType(0);
        boolean result = likeService.save(likes);

        WebSocketServer.OnlineUser onlineUser = new WebSocketServer.OnlineUser(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl()
        );


        WebSocketServer.sendLikeNotification(
                author.getId(),
                onlineUser,
                "post",
                id
        );
        return BaseResponse.success(result);
    }


    /**
     * 获取详细个人信息
     */
    @GetMapping("/userDetail")
    public BaseResponse<UserDetailVO> getUserDetail(HttpServletRequest request) {
        // 1. 获取当前登录用户
        String userInfo = request.getHeader("X-User-Info");
        if (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);
        UserDetailVO userDetailVO = new UserDetailVO();
        userDetailVO.setUser(user);

        LambdaQueryWrapper<Blogs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blogs::getAuthorId, user.getId());
        String subSql = ""
                + "SELECT a.id "
                + "FROM blogs a "
                + "INNER JOIN ("
                + "    SELECT data_id, MAX(version) AS version "
                + "    FROM blogs "
                + "    GROUP BY data_id"
                + ") m ON a.data_id = m.data_id AND a.version = m.version";
        queryWrapper.inSql(Blogs::getId, subSql);
        List<Blogs> blogsList = blogsService.list(queryWrapper);
        userDetailVO.setBlogsList(blogsList);

        LambdaQueryWrapper<Videos> videoQueryWrapper = new LambdaQueryWrapper<>();
        videoQueryWrapper.eq(Videos::getUserId, user.getId());
        List<Videos> videosList = videosService.list(videoQueryWrapper);
        userDetailVO.setVideosList(videosList);

        LambdaQueryWrapper<Questions> questionQueryWrapper = new LambdaQueryWrapper<>();
        questionQueryWrapper.eq(Questions::getUserId, user.getId());
        List<Questions> questionsList = questionsService.list(questionQueryWrapper);
        userDetailVO.setQuestionsList(questionsList);

        LambdaQueryWrapper<Votes> voteQueryWrapper = new LambdaQueryWrapper<>();
        voteQueryWrapper.eq(Votes::getUserId, user.getId());
        List<Votes> votesList = votesService.list(voteQueryWrapper);
        userDetailVO.setVotesList(votesList);

        LambdaQueryWrapper<Follow> fansQueryWrapper = new LambdaQueryWrapper<>();
        fansQueryWrapper.eq(Follow::getUserId, user.getId());
        Follow follow = followService.getOne(fansQueryWrapper);
        if (follow != null && !follow.getFollowIdList().isEmpty()) {
            List<Integer> followIdList = JSONObject.parseArray(follow.getFollowIdList(), Integer.class);
            if (!followIdList.isEmpty()){
                BaseResponse<List<User>> response = userFeignClient.getUserByListId(followIdList);
                if (response.getData()  != null && !response.getData().isEmpty()){
                    userDetailVO.setFansList(response.getData());
                    userDetailVO.setFansCount(followIdList.size());
                }
            }
        }

        List<Follow> list = followService.list();
        ArrayList<User> users = new ArrayList<>();

        for (Follow item : list) {
            String followIdList = item.getFollowIdList();
            if (StringUtils.isNotEmpty(followIdList)){
                List<Integer> followIdList1 = JSONObject.parseArray(followIdList, Integer.class);
                if (followIdList1.contains(user.getId())){
                    BaseResponse<User> response = userFeignClient.getUserById(item.getUserId());
                    if (response.getData() != null){
                        users.add(response.getData());
                    }
                }
            }
        }
        userDetailVO.setFollowList(users);
        userDetailVO.setFollowCount(users.size());



        return BaseResponse.success(userDetailVO);
    }

    /**
     * 博客排行榜
     * @param
     */
    @PostMapping("/rank")
    public BaseResponse<RankVO> getRank(HttpServletRequest request) {
        RankVO rankVO = new RankVO();
        LambdaQueryWrapper<Blogs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Blogs::getViewCount);
        queryWrapper.last("limit 5");
        List<Blogs> blogsList = blogsService.list(queryWrapper);
        blogsList.forEach(item ->{
            item.setContent(null);
            LambdaQueryWrapper<Likes> likesLambdaQueryWrapper = new LambdaQueryWrapper<>();
            likesLambdaQueryWrapper.eq(Likes::getResourceId, item.getId());
            likesLambdaQueryWrapper.eq(Likes::getType, 0);
            item.setLikesCount(likeService.count(likesLambdaQueryWrapper));
        });
        rankVO.setBlogsRanks(blogsList);

        QueryWrapper<Follow> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("user_id") // 选择user_id字段
                .orderByDesc("JSON_LENGTH(follow_id_list)") // 按粉丝数降序排列
                .last("LIMIT 5"); // 限制结果数量为5
        List<Follow> list = followService.list(userQueryWrapper);
        List<User> topUserIds = list.stream()
                .map(follow -> userFeignClient.getUserById(follow.getUserId()).getData())
                .collect(Collectors.toList());
        rankVO.setUsersRanks(topUserIds);
        return BaseResponse.success(rankVO);
    }


    @PostMapping(value ="/getHistoryBlogVersion/{id}")
    public BaseResponse<List<Blogs>> getHistoryBlogVersion(@PathVariable("id") String id) {
        Blogs blogs = blogsService.getById(id);
        if (blogs == null){
            return BaseResponse.error(400, "博客不存在");
        }
        String dataId = blogs.getDataId();
        LambdaQueryWrapper<Blogs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blogs::getDataId, dataId);
        queryWrapper.orderByDesc(Blogs::getVersion);
        List<Blogs> blogsList = blogsService.list(queryWrapper);
        blogsList.remove(0);
        return BaseResponse.success(blogsList);
    }

    @PostMapping(value ="/restore/{id}")
    public BaseResponse<Boolean> restore(@PathVariable("id") String id) {
        Blogs blogs = blogsService.getById(id);
        if (blogs == null){
            return BaseResponse.error(400, "博客不存在");
        }
        String dataId = blogs.getDataId();
        LambdaQueryWrapper<Blogs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blogs::getDataId, dataId);
        queryWrapper.orderByDesc(Blogs::getVersion);
        List<Blogs> blogsList = blogsService.list(queryWrapper);
        Blogs maxVersionBlog = blogsList.get(0);
        blogs.setVersion(maxVersionBlog.getVersion() + 1);
        boolean flag = blogsService.updateById(blogs);
        return BaseResponse.success(flag);
    }




    public static void main(String[] args) {
        System.out.println(IdUtil.fastSimpleUUID());
    }


}
