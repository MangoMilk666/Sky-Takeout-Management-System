package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Objects;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传递的明文密码进行md5加密处理 ，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (Objects.equals(employee.getStatus(), StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 添加员工信息
     * @param employeeDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEmployee(EmployeeDTO employeeDTO) {
        // 已有的属性拷贝
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        // 补充其他的employee对象属性
        // 默认密码123456，加密处理，规范使用常量类
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setStatus(StatusConstant.ENABLE); //默认正常,规范使用常量类
        // 创建时间，修改时间(改用aop实现)
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        // 创建人和修改人id
        //employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.addEmployee(employee);
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult getPage(EmployeePageQueryDTO employeePageQueryDTO) {
        // 分页参数
        // 本质是基于mybatis拦截SQL并且加上'limit offset, pageSize', 但只对第一条SQL生效
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        // 查询, Page类继承了ArrayList，所以可以直接作为返回类型
        Page<Employee> p = employeeMapper.pageQuery(employeePageQueryDTO);
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 禁用/启用员工账号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer status, Long id) {
        Employee employee = Employee.builder()
                            .id(id)
                            .status(status)
                            .build();
        // 更新修改时间和修改员工id(改用aop实现)
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(BaseContext.getCurrentId());
        // 调用一个统一的dao层方法,而不要单独一个
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    /**
     * 编辑(修改)员工信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // 更新时间和修改用户(改用aop实现)
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

}
