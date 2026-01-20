package com.ruoyi.web.controller.hx;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.HxStudentProfile;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.IHxActivityService;
import com.ruoyi.system.service.IHxPointService;
import com.ruoyi.system.service.IHxUserWechatService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 学生管理控制器
 *
 * @author GPT
 */
@RestController
@Validated
@RequestMapping("/hx/student")
public class HxStudentController extends BaseController
{
    @Autowired
    private IHxPointService hxPointService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private IHxUserWechatService hxUserWechatService;
    
    @Autowired
    private IHxActivityService hxActivityService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 学生列表查询
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/list")
    public TableDataInfo studentList(HxStudentProfile profile)
    {
        startPage();
        List<Map<String, Object>> students = hxPointService.selectStudentList(profile);
        return getDataTable(students);
    }

    /**
     * 学生详情查询
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/{userId}")
    public AjaxResult studentDetail(@PathVariable Long userId)
    {
        Map<String, Object> student = hxPointService.selectStudentDetail(userId);
        return success(student);
    }

    /**
     * 更新学生基本信息
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateStudent(@Valid @RequestBody SysUser user)
    {
        sysUserService.updateUser(user);
        return success();
    }

    /**
     * 调整学生积分
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PostMapping("/adjustPoints")
    public AjaxResult adjustPoints(@Valid @RequestBody PointAdjustRequest request)
    {
        hxPointService.adjustPoints(request.getUserId(), request.getPoints(), request.getRemark(), getUsername());
        return success();
    }

    /**
     * 重置学生密码
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@Valid @RequestBody ResetPasswordRequest request)
    {
        // 设置默认密码（如果未提供）
        String password = StringUtils.isNotEmpty(request.getPassword()) 
                ? request.getPassword() 
                : "123456";
        
        // 加密密码
        String encryptedPassword = passwordEncoder.encode(password);
        
        // 更新密码
        sysUserService.resetUserPwd(sysUserService.selectUserById(request.getUserId()).getUserName(), encryptedPassword);
        return success();
    }

    /**
     * 更改学生账号状态
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@Valid @RequestBody ChangeStatusRequest request)
    {
        SysUser user = new SysUser();
        user.setUserId(request.getUserId());
        user.setStatus(request.getStatus());
        sysUserService.updateUserStatus(user);
        return success();
    }

    /**
     * 解绑学生微信
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @DeleteMapping("/unbindWechat/{userId}")
    public AjaxResult unbindWechat(@PathVariable Long userId)
    {
        hxUserWechatService.unbindUserWechat(userId);
        return success();
    }

    /**
     * 批量重置密码
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PostMapping("/batch/resetPwd")
    public AjaxResult batchResetPwd(@Valid @RequestBody BatchResetPasswordRequest request)
    {
        String password = StringUtils.isNotEmpty(request.getPassword()) 
                ? request.getPassword() 
                : "123456";
        String encryptedPassword = passwordEncoder.encode(password);
        
        for (Long userId : request.getUserIds())
        {
            sysUserService.resetUserPwd(sysUserService.selectUserById(userId).getUserName(), encryptedPassword);
        }
        return success();
    }

    /**
     * 批量解绑微信
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PostMapping("/batch/unbindWechat")
    public AjaxResult batchUnbindWechat(@Valid @RequestBody BatchRequest request)
    {
        for (Long userId : request.getUserIds())
        {
            hxUserWechatService.unbindUserWechat(userId);
        }
        return success();
    }

    /**
     * 获取学生活动记录
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/activities")
    public TableDataInfo studentActivities(@RequestParam Long userId)
    {
        startPage();
        List<Map<String, Object>> activities = hxActivityService.selectStudentActivityList(userId);
        return getDataTable(activities);
    }

    /**
     * 积分调整请求
     */
    public static class PointAdjustRequest
    {
        @NotNull(message = "用户ID不能为空")
        private Long userId;

        @NotNull(message = "积分变动值不能为空")
        private Integer points;

        private String remark;

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(Long userId)
        {
            this.userId = userId;
        }

        public Integer getPoints()
        {
            return points;
        }

        public void setPoints(Integer points)
        {
            this.points = points;
        }

        public String getRemark()
        {
            return remark;
        }

        public void setRemark(String remark)
        {
            this.remark = remark;
        }
    }

    /**
     * 重置密码请求
     */
    public static class ResetPasswordRequest
    {
        @NotNull(message = "用户ID不能为空")
        private Long userId;

        private String password;

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(Long userId)
        {
            this.userId = userId;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }
    }

    /**
     * 更改状态请求
     */
    public static class ChangeStatusRequest
    {
        @NotNull(message = "用户ID不能为空")
        private Long userId;

        @NotNull(message = "状态不能为空")
        private String status;

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(Long userId)
        {
            this.userId = userId;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }
    }

    /**
     * 批量请求
     */
    public static class BatchRequest
    {
        @NotEmpty(message = "用户ID列表不能为空")
        private List<Long> userIds;

        public List<Long> getUserIds()
        {
            return userIds;
        }

        public void setUserIds(List<Long> userIds)
        {
            this.userIds = userIds;
        }
    }

    /**
     * 批量重置密码请求
     */
    public static class BatchResetPasswordRequest
    {
        @NotEmpty(message = "用户ID列表不能为空")
        private List<Long> userIds;

        private String password;

        public List<Long> getUserIds()
        {
            return userIds;
        }

        public void setUserIds(List<Long> userIds)
        {
            this.userIds = userIds;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }
    }
}