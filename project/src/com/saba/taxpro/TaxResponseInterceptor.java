package com.saba.taxpro;

import in.amitv.mongowork.dto.TaxRecDTO;
import in.amitv.mongowork.service.MongoHelperUtil;

import java.util.Date;
import java.util.logging.Logger;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.message.Message;

public class TaxResponseInterceptor extends LoggingOutInterceptor {

	Message msg;
	TaxRecDTO dto;
	
	@Override
	public void handleMessage(Message message) throws Fault {
		msg = message;
		dto = (TaxRecDTO) msg.getExchange().getInMessage().remove("in.amitv.mongowork.dto.taxrec");
		super.handleMessage(message);
	}
	
	@Override
	protected void log(Logger logger, String message) {
		MongoHelperUtil.addTaxRecToMongo(dto);
	}
	
	@Override
	protected String formatLoggingMessage(LoggingMessage loggingMessage) {
		dto.getResp().setContentType(loggingMessage.getContentType().toString());
		dto.getResp().setHeaders(loggingMessage.getHeader().toString());
		dto.getResp().setPayload(loggingMessage.getPayload().toString());
		dto.getResp().setResponseCode(loggingMessage.getResponseCode().toString());
		dto.getResp().setTs(new Date());
		return "";
	}
	
}
