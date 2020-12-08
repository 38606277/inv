
    /**  
    * @Description: TODO(用一句话描述该文件做什么)
    * @author markzgwu
    * @date 2018年6月26日
    * @version V1.0  
    */
    
package root.report.leeutils;

import cn.hutool.log.LogFactory;

    /**
     * @author markzgwu
     *
     */
    public class IDUtil {
        static final SnowflakeIdWorker ID_WORKER = new SnowflakeIdWorker(0, 0);
        public static long getId() {
            final long id = ID_WORKER.nextId();
            return id;
        }

        /**
         * @param args
         */
        public static void main(String[] args) {
            LogFactory.get().info("id="+getId());
        }

    }
