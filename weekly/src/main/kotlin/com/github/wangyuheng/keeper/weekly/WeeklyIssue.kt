package com.github.wangyuheng.keeper.weekly

import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

private val extensions = listOf(TablesExtension.create())

private val parser = Parser.builder()
        .extensions(extensions)
        .build()
private val renderer = HtmlRenderer.builder()
        .extensions(extensions)
        .build()

class WeeklyIssue(var projectId: Int, var issueId: Int, var name: String, var description: String) {
    fun html(): String = renderer.render(parser.parse(this.description))
}
