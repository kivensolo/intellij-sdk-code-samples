# Facet Basics [![JetBrains IntelliJ Platform SDK Docs](https://jb.gg/badges/docs.svg)][docs]
*Reference: [Facet in IntelliJ SDK Docs][docs:facet_basics]*

## Quickstart
在 IntelliJ IDEA 等集成开发环境 (IDE) 中，**Facet** 指的是模块级别的配置工具(`Project Settings > Facets`)。<br>
通过提供额外的库、依赖项、技术和用于配置特定于框架的设置的UI元素，用额外的框架支持扩展IDE的基本特性。

Facet指定了模块(Module)使用的框架或技术, 每个 Facet 对应于一个特定的技术栈或框架，提供对该技术栈的支持和配置选项。

一个模块(Module)可以使用多个Facet，例如，同时选择Spring框架中的spring-mvc和spring-security。

Facet允许我们指定由`FacetConfiguration` 实现指定的任何配置 - 在本例中是 SDK 的路径。

### Extension Points

| Name                     | Implementation                      | Extension Point Class |
|--------------------------|-------------------------------------|-----------------------|
| `com.intellij.facetType` | [DemoFacetType][file:DemoFacetType] | `FacetType`           |

*Reference: [Plugin Extension Points in IntelliJ SDK Docs][docs:ep]*


[docs]: https://plugins.jetbrains.com/docs/intellij/
[docs:facet_basics]: https://plugins.jetbrains.com/docs/intellij/facet.html
[docs:ep]: https://plugins.jetbrains.com/docs/intellij/plugin-extensions.html

[file:DemoFacetType]: ./src/main/java/org/intellij/sdk/facet/DemoFacetType.java

