# database-base
## 目标
### 终极目标
接口上传类型参数，实现从数据库或者网络请求中获取特定类型的数据

实现通用的数据库初始化（数据库连接池等）
完成数据库查询与类型绑定的框架，能实现传入参数调用指定的service

mybatis 中会将 mapper 注册成 bean，hibernate 会将 repository 注册成 bean。<br/>
创建一个类名与上述 bean 的映射关系。<br/>


    question：
    
        不同的 mapper 有不同的方法，倘若未实现怎么处理?
        ----------只有特定标记（实现接口或者注解）的 bean 可以，且他们一定有子类。
        整合接口需要指定类型，才能引用特定实现。方法多一个参数。