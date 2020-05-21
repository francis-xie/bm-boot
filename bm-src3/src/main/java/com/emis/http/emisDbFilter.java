package com.emis.http;

/*
* Track+[14915] dana.gao 2010/05/19 改善連線池,增加未關閉連接查找功能
* */

import javax.servlet.*;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import com.emis.db.emisDb;

import java.io.IOException;

public class emisDbFilter implements Filter {
	
	public void doFilter(ServletRequest req, ServletResponse res,  FilterChain chain) throws IOException, ServletException {

    emisDb.startCheckPoint(this, req);
    chain.doFilter(req, res);
    emisDb.endCheckPoint(this, req);
	}



	public void init(FilterConfig filterConfig) {
		// noop
	}



	public void destroy() {
		// noop
	}
}

