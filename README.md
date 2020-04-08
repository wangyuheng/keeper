# keeper
keeper for team running!

## feature

### 1. 成员负载可视化

![workload 0](https://raw.githubusercontent.com/wangyuheng/keeper/master/.design/workload_0.png)

![workload diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/wangyuheng/keeper/master/.plantuml/workload.puml)

通过图表直观展示团队成员工作负载、贡献、以及项目进度等信息
核心目的: 细化工作量, 通过可量化、可追踪的方式, 把控项目进度

高价值指标包括: 

1. 成员当前issue数量
2. 当前版本项目issue进度及分布
3. 成员参与issue数量
4. bug数量趋势

### 2. 定时周报发送

![weekly diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/wangyuheng/keeper/master/.plantuml/weekly.puml)

1. 督促team成员发送周报，否则会发送remind邮件
2. 简化编写成本，通过 **issue template** & **markdown** 语法，专注于周报内容本身
3. 统一管理主题、收件人等信息
4. 归档管理周报信息，追踪周报中的问题

### 3. Dingding消息@人

![workload diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/wangyuheng/keeper/master/.plantuml/alert.puml)

通过图表直观展示团队成员工作负载、贡献、以及项目进度等信息
监听`Gitlab Hook`事件, 紧急消息通过`钉钉机器人`发送至钉钉群组, 并`@(提醒)`相关方. 同时自动流转issue的`pipeline`.

1. 紧急事项实时提醒到人
2. 定制pipeline & 自动流转

#### 3.1. 为什么不通过钉钉自带的gitlab机器人 或者 gitlab notify email？

1. 发送的消息过多, 会忽略有意义的信息
2. 不能 @ 到相关的人, 达不到提醒的作用

### 4. CAS单点登录

![cas diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/wangyuheng/keeper/master/.plantuml/cas.puml)

将Gitlab作为授权服务器，通过代码实现Gitlab Applications交互，并调用API获取用户信息。

## dependent

1. [metabase](https://github.com/metabase/metabase)
2. [dingtalk robot](https://ding-doc.dingtalk.com/doc#/serverapi2/qf2nxq)
3. [gitlab api](https://docs.gitlab.com/ee/api/issues.html)