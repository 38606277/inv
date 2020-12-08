package root.report.http;

import com.cmcc.mss.msgheader.MsgHeader;
import com.cmcc.mss.oa_pageinquirydepartmentinfosrv.*;
import com.cmcc.mss.oa_pageinquirydeptemprelationinfosrv.*;
import com.cmcc.mss.oa_pageinquiryemployeeinfosrv.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OaController extends RO {
    private static final Logger log = Logger.getLogger(OaController.class);

    @Scheduled(cron = "0 0 1 * * ?")
    public String syncEmployee() throws Exception{
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            sqlSession.delete("oa.deleteEmployee");
            OAPageInquiryEmployeeInfoSrv_Service ss = new OAPageInquiryEmployeeInfoSrv_Service();
            OAPageInquiryEmployeeInfoSrv port = ss.getOAPageInquiryEmployeeInfoSrvPort();
            OAPageInquiryEmployeeInfoSrvRequest requestParam = new OAPageInquiryEmployeeInfoSrvRequest();
            MsgHeader header = new MsgHeader();
            String value = DbFactory.Open(DbFactory.FORM).selectOne("budget.getDicValueByKey","sourcesystemid");
            header.setSOURCESYSTEMID(value);
            header.setSOURCESYSTEMNAME("报表平台");
            header.setUSERID("报表平台");
            header.setUSERNAME("报表平台");
            BigDecimal per_page = new BigDecimal(10000);//默认每次提交1W条数据
            BigDecimal current_page = new BigDecimal(1);
            BigDecimal total_size = new BigDecimal(-1);
            do {
                header.setPAGESIZE(per_page);
                header.setCURRENTPAGE(current_page);
                header.setTOTALRECORD(new BigDecimal(-1));
                requestParam.setMsgHeader(header);
                OAPageInquiryEmployeeInfoSrvResponse responseData = port.process(requestParam);
                current_page = current_page.add(new BigDecimal(1));
                total_size = responseData.getTOTALRECORD();
                List<OAPageInquiryEmployeeInfoSrvOutputItem> list = responseData.getOAPageInquiryEmployeeInfoSrvOutputCollection().getOAPageInquiryEmployeeInfoSrvOutputItem();
                sqlSession.insert("oa.batchInsertEmployees", list);
            }
            while (total_size.intValue() != -1 &&(total_size.intValue() > (current_page.intValue()-1) * per_page.intValue()));
        }catch(Exception e){
            log.error("同步OA用户信息失败:"+e.getMessage());
            sqlSession.getConnection().rollback();
        }finally{
            sqlSession.getConnection().setAutoCommit(true);
        }
        return SuccessMsg("同步OA用户信息成功",null);
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public String syncDepartment() throws Exception{
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            sqlSession.delete("oa.deleteDepartment");
            OAPageInquiryDepartmentInfoSrv_Service ss = new OAPageInquiryDepartmentInfoSrv_Service();
            OAPageInquiryDepartmentInfoSrv port = ss.getOAPageInquiryDepartmentInfoSrvPort();
            OAPageInquiryDepartmentInfoSrvRequest requestParam = new OAPageInquiryDepartmentInfoSrvRequest();
            MsgHeader header = new MsgHeader();
            String value = DbFactory.Open(DbFactory.FORM).selectOne("budget.getDicValueByKey","sourcesystemid");
            header.setSOURCESYSTEMID(value);
            header.setSOURCESYSTEMNAME("报表平台");
            header.setUSERID("报表平台");
            header.setUSERNAME("报表平台");
            BigDecimal per_page = new BigDecimal(10000);//默认每次提交1W条数据
            BigDecimal current_page = new BigDecimal(1);
            BigDecimal total_size = new BigDecimal(-1);
            do {
                header.setPAGESIZE(per_page);
                header.setCURRENTPAGE(current_page);
                header.setTOTALRECORD(new BigDecimal(-1));
                requestParam.setMsgHeader(header);
                OAPageInquiryDepartmentInfoSrvResponse responseData = port.process(requestParam);
                current_page = current_page.add(new BigDecimal(1));
                total_size = responseData.getTOTALRECORD();
                List<OAPageInquiryDepartmentInfoSrvOutputItem> list = responseData.getOAPageInquiryDepartmentInfoSrvOutputCollection().getOAPageInquiryDepartmentInfoSrvOutputItem();
                sqlSession.insert("oa.batchInsertDepartment", list);
            }
            while (total_size.intValue() != -1 &&(total_size.intValue() > (current_page.intValue()-1) * per_page.intValue()));
        }catch(Exception e){
            log.error("同步OA部门信息失败:"+e.getMessage());
            sqlSession.getConnection().rollback();
        }finally{
            sqlSession.getConnection().setAutoCommit(true);
        }
        return SuccessMsg("同步OA部门信息成功",null);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public String syncDeptEmpRelation() throws Exception{
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            sqlSession.delete("oa.deleteDeptEmpRelation");
            OAPageInquiryDeptEmpRelationInfoSrv_Service ss = new OAPageInquiryDeptEmpRelationInfoSrv_Service();
            OAPageInquiryDeptEmpRelationInfoSrv port = ss.getOAPageInquiryDeptEmpRelationInfoSrvPort();
            OAPageInquiryDeptEmpRelationInfoSrvRequest requestParam = new OAPageInquiryDeptEmpRelationInfoSrvRequest();
            MsgHeader header = new MsgHeader();
            String value = DbFactory.Open(DbFactory.FORM).selectOne("budget.getDicValueByKey","sourcesystemid");
            header.setSOURCESYSTEMID(value);
            header.setSOURCESYSTEMNAME("报表平台");
            header.setUSERID("报表平台");
            header.setUSERNAME("报表平台");
            BigDecimal per_page = new BigDecimal(10000);//默认每次提交1W条数据
            BigDecimal current_page = new BigDecimal(1);
            BigDecimal total_size = new BigDecimal(-1);
            do {
                header.setPAGESIZE(per_page);
                header.setCURRENTPAGE(current_page);
                header.setTOTALRECORD(new BigDecimal(-1));
                requestParam.setMsgHeader(header);
                OAPageInquiryDeptEmpRelationInfoSrvResponse responseData = port.process(requestParam);
                current_page = current_page.add(new BigDecimal(1));
                total_size = responseData.getTOTALRECORD();
                List<OAPageInquiryDeptEmpRelationInfoSrvOutputItem> list = responseData.getOAPageInquiryDeptEmpRelationInfoSrvOutputCollection().getOAPageInquiryDeptEmpRelationInfoSrvOutputItem();
                sqlSession.insert("oa.batchInsertDeptEmpRelation", list);
            }
            while (total_size.intValue() != -1 &&(total_size.intValue() > (current_page.intValue()-1) * per_page.intValue()));
        }catch(Exception e){
            log.error("同步OA用户部门信息失败:"+e.getMessage());
            sqlSession.getConnection().rollback();
        }finally{
            sqlSession.getConnection().setAutoCommit(true);
        }
        return SuccessMsg("同步OA用户部门信息成功",null);
    }
}
