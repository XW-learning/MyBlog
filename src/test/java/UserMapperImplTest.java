import com.personalblog.mapper.impl.UserMapperImpl;
import com.personalblog.model.User;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class UserMapperImplTest {

    // 实例化 Mapper，就像在 Service 层调用它一样
    private UserMapperImpl userMapperImpl = new UserMapperImpl();

    /**
     * 测试 1：模拟用户注册 (插入数据)
     */
    @Test
    public void testSaveUser() {
        System.out.println("\n====== 测试用户注册 ======");

        // 1. 准备一个假用户数据
        User user = new User();
        user.setUsername("junit_test_user"); // 测试账号
        user.setPassword("123456");          // 测试密码
        user.setNickname("测试员小张");
        user.setCreateTime(new Date());      // 当前时间

        // 2. 调用 Mapper 的 save 方法
        boolean isSuccess = userMapperImpl.save(user);

        // 3. 验证结果
        if (isSuccess) {
            System.out.println("✅ 注册成功！用户信息如下：");
        } else {
            System.err.println("❌ 注册失败！请检查 JDBCUtils 或 SQL 语句。");
        }
    }

    /**
     * 测试 2：模拟用户登录 (查询数据)
     */
    @Test
    public void testFindUser() {
        System.out.println("\n====== 测试用户登录 ======");

        // 1. 模拟前端传来的用户名
        String searchUsername = "junit_test_user"; // 使用上面刚才插入的账号

        // 2. 调用 Mapper 查询
        User user = userMapperImpl.findByUsername(searchUsername);

        // 3. 验证结果
        if (user != null) {
            System.out.println("✅ 登录成功！用户信息如下：");
            System.out.println("   ID: " + user.getId());
            System.out.println("   账号: " + user.getUsername());
            System.out.println("   昵称: " + user.getNickname());
            System.out.println("   密码: " + user.getPassword());
        } else {
            System.err.println("❌ 登录失败！请检查 JDBCUtils 或 SQL 语句。");
        }
    }
}