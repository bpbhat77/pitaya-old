package com.emo.pitaya.email.html;

class TerminalHtmlContent implements HtmlContent {

	private final String content;
	
	TerminalHtmlContent(final String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return content;
	}
}
