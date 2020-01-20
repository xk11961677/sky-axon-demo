## sky-axon-demo 项目
[![license](https://img.shields.io/badge/license-MIT-ff69b4.svg)](https://mit-license.org/license.html)
[![springcloud](https://img.shields.io/badge/springcloud-greenwich-orange.svg)](https://spring.io/projects/spring-cloud)
[![axon](https://img.shields.io/badge/axon-4.2.1-orange.svg)](https://axoniq.io/)
![Gitter](https://img.shields.io/gitter/room/sky-docs/community)
![version](https://img.shields.io/badge/version-0.0.1-blueviolet.svg)
![helloworld](https://img.shields.io/badge/hello-world-blue)


### 项目介绍
```
功能点：
    
技术点：
	
```
### 代码结构层
```

│  ├─sky-axon-api------------------接口api模块                          
│  │
│  ├─sky-axon-bom------------------依赖jar模块
│  │
│  ├─sky-axon-common---------------公共组件模块
│  │
│  ├─sky-axon-event----------------事件模块
│  │
│  ├─sky-axon-query----------------查询模块
│  │
│  ├─sky-axon-web------------------命令模块
│  │
│  │
    
```

### 使用指南
* 运行sky-axon-web模块

### 版本说明
* 暂时0.0.1版本

### 版权说明
* [The MIT License (MIT)](LICENSE)

## 问题反馈
* 在使用中有任何问题，欢迎反馈

## 开发计划


## 关于作者

* name:  sky 
* email: shen11961677@163.com

## 其他说明
```
   需求1: 
       1.1. 查询某个聚合ID所有事件
       1.2. 根据 步骤1 手动打快照且有版本或标签号 ,根据标签或版本溯源
   
   需求2:     
       2.1. 查询快照所有 [版本] 或 [标签]
       
   扩展axon:
       1. 默认查询将带有 版本 或 标签  的快照不能查询出来
       2. 快照不能将 扩展的标签 与 版本号 的快照数据覆盖

```


### git message 规约
#### 作用
* 生成规范的 changelog 文件
#### 提交格式
* [请点我](docs/script/changelog/commit.md)
#### 插件
* idea 可使用 git commit template 插件
* npm 可以使用 commitizen

#### 生成changelog方式
* 运行docs/script/changelog/gitlog.sh
