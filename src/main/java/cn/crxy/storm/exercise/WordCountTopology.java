package cn.crxy.storm.exercise;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.io.FileUtils;



import com.lmax.disruptor.SleepingWaitStrategy;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

/**
 * 作业：实现单词计数。
 *     (1)要求从一个文件夹中把所有文件都读取，计算所有文件中的单词出现次数。
 *     (2)当文件夹中的文件数量增加是，实时计算所有文件中的单词出现次数。
 */
public class WordCountTopology 
{
	
	public static class DataSourceSpout extends BaseRichSpout{
		private Map conf;
		private TopologyContext context;
		private SpoutOutputCollector collector;
		
		final Random random = new Random();
		/**
		 * 在本实例运行时，首先被调用
		 */
		public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
			this.conf = conf;
			this.context = context;
			this.collector = collector;
		}
		
		/**
		 * 认为heartbeat，永无休息，死循环的调用。线程安全的操作。
		 */
//		public void nextTuple() {
//			//读取目标文件夹中新产生的文件(们)
//			Collection<File> listFiles = FileUtils.listFiles(new File("D:\\test"), new String[]{"txt"}, true);
//			//把每个文件中的每一行解析出来
//			for (File file : listFiles) {
//				try {
//					List<String> lines = FileUtils.readLines(file);
//					//把每一行发射出去
//					for (String line : lines) {
//						this.collector.emit(new Values(line));
//					}
//					FileUtils.moveFile(file, new File(file.getAbsolutePath()+"."+System.currentTimeMillis()));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
//			}
//		}

		
		
		int i = 0;
		
		public void nextTuple() {
			
			collector.emit( new Values(i),i);
			
			collector.emit( new Values(i),i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
		}

		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			//Fields是一个field的List
			declarer.declare(new Fields("line"));
		}
		
		
		@Override
		public void ack(Object msgId) {
//			// TODO Auto-generated method stub
//			super.ack(msgId);
			System.err.println("ack beg");
			System.err.println("ack" + msgId);
			
			System.err.println("ack end");
		}
		
		@Override
		public void fail(Object msgId) {
			// TODO Auto-generated method stub
			System.err.println("fail beg");
			System.err.println("fail"+  msgId);
			System.err.println("fail end");
		}
	}
	
	public static class SplitBolt extends BaseRichBolt{
		private Map conf;
		private TopologyContext context;
		private OutputCollector collector;
		
		public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
			this.conf = conf;
			this.context = context;
			this.collector = collector;
		}
		
		/**
		 * 拆分每一行单词
		 */
//		public void execute(Tuple tuple) {
//			//读取tuple
//			String line = tuple.getStringByField("line");
//			//拆分每一行，得到一个个单词
//			String[] words = line.split("\\s");
//			//把单词发射出去
//			for (String word : words) {
//				this.collector.emit(new Values(word));
//			}
//		}
		
		
		public void execute(Tuple tuple) {
			Long value = tuple.getLongByField("line");
			if (value % 2 == 0) {
				collector.ack(tuple);
				
			}else {
				collector.fail(tuple);
			}


		}

		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word"));
		}

	}
	
	public static class CountBolt extends BaseRichBolt{
		private Map conf;
		private TopologyContext context;
		private OutputCollector collector;
		
		public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
			this.conf = conf;
			this.context = context;
			this.collector = collector;
		}
		
		/**
		 * 对单词进行计数
		 */
		
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		public void execute(Tuple tuple) {
			//读取tuple
			String word = tuple.getStringByField("word");
			//保存每个单词
			Integer value = countMap.get(word);
			if(value==null){
				value = 0;
			}
			value++;
			countMap.put(word, value);
			//把结果写出去
			System.err.println("============================================");
			Utils.sleep(2000);
			for (Entry<String, Integer> entry : countMap.entrySet()) {
				System.out.println(entry);
			}
		}

		public void declareOutputFields(OutputFieldsDeclarer arg0) {
			
		}

	}
	
    public static void main( String[] args ) 
    {
    	String DATASOURCE_SPOUT = DataSourceSpout.class.getSimpleName();
    	String SPLIT_BOLD = SplitBolt.class.getSimpleName();
    	String COUNT_BOLT = CountBolt.class.getSimpleName();
    	
        final TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(DATASOURCE_SPOUT, new DataSourceSpout());
        builder.setBolt(SPLIT_BOLD, new SplitBolt()).shuffleGrouping(DATASOURCE_SPOUT);
        builder.setBolt(COUNT_BOLT, new CountBolt()).shuffleGrouping(SPLIT_BOLD);
        
        final LocalCluster localCluster = new LocalCluster();
        final Config config = new Config();
		localCluster.submitTopology(WordCountTopology.class.getSimpleName(), config, builder.createTopology());
		Utils.sleep(9999999);
		localCluster.shutdown();
    }
}
