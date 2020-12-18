package root.report.common;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import root.report.db.DbFactory;
import root.report.sys.SysContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public  class DbSession {

   public static  SqlSession getDefaultSession() {
       SqlSession sqlSession = null;
       try {
           sqlSession = DbFactory.Open(DbFactory.FORM);
           return sqlSession;
       } catch (Exception ex) {
           throw ex;
       }
   }

   public static  <E> List<E>   selectList(String mapper, Object param) {
       SqlSession sqlSession = null;
       try {
           sqlSession = DbFactory.Open(DbFactory.FORM);
           return  sqlSession.selectList(mapper, param);
       } catch (Exception ex) {
           throw ex;
       }
   }
   public static List selectList(String mapper, Map param, RowBounds rowBounds) {
       SqlSession sqlSession = null;
       try {
           sqlSession = DbFactory.Open(DbFactory.FORM);
           List<Object> list = sqlSession.selectList(mapper, param,rowBounds);
           return list;
       } catch (Exception ex) {
           throw ex;
       }
   }

   public static List selectListByAuth(String mapperId, Map param) {
      SqlSession sqlSession = null;
       //注入行权限过滤	param.put("org_ids",dataAuth);
       ////		param.put("dept_ids",dataAuth);
//        List result = DbFactory.Open(DbFactory.FORM).selectList(mapperId, param);
//		列权限过滤
//        return result;


       try {
           sqlSession = DbFactory.Open(DbFactory.FORM);
           String userCode = SysContext.getUserCode();
           Map authData =DbSession.getDataAuthByUsercode(userCode);
           param.putAll(authData);
           List<Map<String, Object>> list = sqlSession.selectList(mapperId, param);
           return list;
       } catch (Exception ex) {
           throw ex;
       }
   }

   public static  <T> T selectOne(String mapper, Object param) {
       SqlSession sqlSession = null;
       try {
           sqlSession = DbFactory.Open(DbFactory.FORM);
           return sqlSession.selectOne(mapper, param);
       } catch (Exception ex) {
           throw ex;
       }
   }


   public static  int insert(String mapperId, Map param) {
       SqlSession sqlSession = null;
       try {
           sqlSession = DbFactory.Open(DbFactory.FORM);
           return  sqlSession.insert(mapperId, param);
       } catch (Exception ex) {
           throw ex;
       }
   }

   public static int update(String mapperId, Map param) {
       SqlSession sqlSession = null;
       try {
           sqlSession = DbFactory.Open(DbFactory.FORM);
           return sqlSession.update(mapperId, param);
       } catch (Exception ex) {
           throw ex;
       }
   }

   public static int delete(String mapperId, Map param) {
       SqlSession sqlSession = null;
       try {
           sqlSession = DbFactory.Open(DbFactory.FORM);
          return sqlSession.delete(mapperId, param);
       } catch (Exception ex) {
           throw ex;
       }
   }

    public static Map getDataAuthByUsercode(String userCode){
       Map map=new HashMap();
       DbSession.selectList("",userCode);

       return map;
    }


}
