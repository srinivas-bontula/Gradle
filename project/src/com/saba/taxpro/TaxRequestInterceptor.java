package com.saba.taxpro;

import in.amitv.mongowork.dto.TaxRecDTO;

import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.message.Message;
import org.springframework.util.StringUtils;

public class TaxRequestInterceptor extends LoggingInInterceptor {

	Message msg;
	TaxRecDTO dto;
	
	@Override
	public void handleMessage(Message message) throws Fault {
		msg = message;
		dto = new TaxRecDTO();
		super.handleMessage(message);
	}
	
	@Override
	protected void log(Logger logger, String message){
		HttpServletRequest req = (HttpServletRequest) msg.get("HTTP.REQUEST");
		StringBuilder remoteInfo = new StringBuilder();
		if(StringUtils.isEmpty(req.getRemoteHost())){
			remoteInfo.append("Host : "+req.getRemoteHost());
		}
		if(StringUtils.isEmpty(req.getRemotePort())){
			remoteInfo.append("#Port : "+req.getRemotePort());
		}
		if(StringUtils.isEmpty(req.getRemoteAddr())){
			remoteInfo.append("#IP : "+req.getRemoteAddr());
		}
		if(StringUtils.isEmpty(req.getRemoteUser())){
			remoteInfo.append("#User : "+req.getRemoteUser());
		}
		dto.getReq().setRemoteInfo(remoteInfo.toString());
		msg.put("in.amitv.mongowork.dto.taxrec", dto);
	}
	
	@Override
	protected String formatLoggingMessage(LoggingMessage loggingMessage) {
		dto.getReq().setAddress(loggingMessage.getAddress().toString());
		dto.getReq().setContentType(loggingMessage.getContentType().toString());
		dto.getReq().setHeaders(loggingMessage.getHeader().toString());
		dto.getReq().setMethod(loggingMessage.getHttpMethod().toString());
		dto.getReq().setPayload(loggingMessage.getPayload().toString());
		dto.getReq().setTs(new Date());
		return "";
	}
	
}
