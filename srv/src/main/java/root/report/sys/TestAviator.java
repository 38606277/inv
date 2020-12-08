package root.report.sys;

public class TestAviator {
	public static void main(String[] args) {
//		 String expression = "a";
//	        // 编译表达式
//	        Expression compiledExp = AviatorEvaluator.compile(expression);
//	       List<String> nList= compiledExp.getVariableNames();
//	       System.out.println(nList.toString());		
//	   
//	        Map<String, Object> env = new HashMap<String, Object>();
//	        env.put("a", 100);
//	        env.put("b", 45);
//	        env.put("c", 4);
//	        // 执行表达式
//	        Object result = compiledExp.execute(env);
//	        System.out.println(result);  // falseCST 2016
		
		String str = "5114";
        String[] strs = str.split("\\+|\\-|\\*|\\/");
        for(String s : strs){
            System.out.println(s);
        }
	}
}
