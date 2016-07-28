package com.hkl.hadoop.yarn.examples;

/**
 * Created by hkl on 16. 7. 27.
 */

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

import java.io.IOException;
import java.util.*;

public class MyClient
{
	private static final Log LOG = LogFactory.getLog(MyClient.class);

	// 클라이언트의 시작시간
	private final long clientStartTime = System.currentTimeMillis();

	// 리소스매니저와 통신을 사용하기위한 YarnClient 객체 선언. 
	private YarnClient yarnClient;
	// YarnClient를 생성할 때 Configuration 객체가 필요.
	private Configuration conf;

	// 애플리케이션 이름 등록
	private String appName = "";

	// 애플리케이션마스터의 실행 우선순위
	private int amPriority = 0;

	// 어플리케이션 마스터가 사용할 큐
	private String amQueue = "";

	// 애플리케이션마스터를 실행하기 위해 요청할 메모리 크기. 기본값 10MB
	private int amMemory = 10;

	// 애플리케이션마스터를 실행하기 위해 요청할 CPU 코어 개수. 기본값 1
	private int amVCores = 1;

	// 애플리케이션마스터가 참조할 JAR 파일 경로
	private String appMasterJarPath = "";

	// 컨테이너 요청 우선순위
	private int requestPriority = 0;

	// HelloYarn을 실행할 컨테이너에게 요청할 메모리 크기. 기본값 10MB
	private int containerMemory = 10;

	// HelloYarn을 실행할 컨테이너에게 요청할 CPU 코어 개수. 기본값 1
	private int containerVirtualCores = 1;

	// HelloYarn을 실행할 컨테이너 개수
	private int numContainers = 1;

	// 클라이언트의 타임아웃 대기 시간(단위는 밀리초)
	private long clientTimeout = 600000;

	private Options opts;


	/*	생성자	    */
	public MyClient() throws Exception
	{
		createYarnClient();
		initOptions();
	}

	private void createYarnClient()
	{
		yarnClient = YarnClient.createYarnClient();
		this.conf = new YarnConfiguration();
		yarnClient.init(conf);
	}

	private void initOptions()
	{
		opts = new Options();
		opts.addOption("appname",		true,	"Application Name. Default value - HelloYarn");
		opts.addOption("priority", 		true,	"Application Priority. Default 0");
		opts.addOption("queue",			true,	"RM Queue in which this application is to be submitted");
		opts.addOption("timeout",		true,	"Application timeout in milliseconds");
		opts.addOption("master_memory",		true,	"Amount of memory in MB to be requested to run the application master");
		opts.addOption("master_vcores",		true,	"Amount of virtual cores to be requested to run the application master");
		opts.addOption("jar",			true,	"Jar file containing the application master");
		opts.addOption("container_memory", 	true,	"Amount of memory in MB to be requested to run the HelloYarn");
		opts.addOption("container_vcores",	true,	"Amount of virtual cores to be requested to run the HelloYarn");
		opts.addOption("num_containers",	true,	"No. of containers on which the HelloYarn needs to be executed");
		opts.addOption("help", 			false,	"Print usage");
	}

	private void printUsage()
	{
		new HelpFormatter().printHelp("Client", opts);
	}

	
	public boolean init(String[] args) throws ParseException
	{
		CommandLine cliParser = new GnuParser().parse(opts, args);

		if(args.length == 0)
		{
			throw new IllegalArgumentException("No args specified for client to initialize");
		}

		if(cliParser.hasOption("help"))
		{
			printUsage();
			return false;
		}

		appName = cliParser.getOptionValue("appname", "HelloYarn");
		amPriority = Integer.parseInt(cliParser.getOptionValue("priority", "0"));
		amQueue = cliParser.getOptionValue("queue", "default");
		amMemory = Integer.parseInt(cliParser.getOptionValue("master_memory", "10"));
		amVCores = Integer.parseInt(cliParser.getOptionValue("master_vcores", "1"));

		if(amMemory < 0)
		{
			throw new IllegalArgumentException("Invalid memory specified for application master, exiting."
					+ " Specified memory = " + amMemory);
		}
		if(amVCores < 0)
		{
			throw new IllegalArgumentException("Invalud virtual cores specified for application master, exiting."
					+ " Specified virtual cores =" + amVCores);
		}

		if(!cliParser.hasOption("jar"))
		{
			throw new IllegalArgumentException("No jar file specified for application master");
		}

		appMasterJarPath = cliParser.getOptionValue("jar");

		containerMemory = Integer.parseInt(cliParser.getOptionValue("container_memory", "10"));
		containerVirtualCores = Integer.parseInt(cliParser.getOptionValue("container_vcores", "1"));
		numContainers = Integer.parseInt(cliParser.getOptionValue("num_containers", "1"));

		if(containerMemory < 0 || containerVirtualCores < 0 || numContainers < 1)
		{
			throw new IllegalArgumentException("Invalid no. of containers or container memory/vcores specified," 
					+ "exiting. "
					+ "Specified containerMemory=" + containerMemory
					+ ", containerVirtualCores=" + containerVirtualCores
					+ ", numContainer=" + numContainers);
		}

		clientTimeout = Integer.parseInt(cliParser.getOptionValue("timeout", "600000"));
		return true;
	}

	/*	애플리케이션 실행을 요청하기 위한 준비 끝	*/


	/*	애플리케이션 요청 기능 구현(MyClient의 핵심 기능)	*/
	public boolean run() throws IOException, YarnException
	{
		LOG.info("Running Client");
		yarnClient.start();

		//신규 어플리케이션ID를 조회
		YarnClientApplication app = yarnClient.createApplication();
		GetNewApplicationResponse appResponse = app.getNewApplicationResponse();

		// 메모리 비교
		int maxMem = appResponse.getMaximumResourceCapability().getMemory();
		LOG.info("MAX mem capabililty of resources in this cluster " + maxMem);
		if(amMemory > maxMem)
		{
			LOG.info("AM memory specified above max threshold of cluster. Using max value."
					+ ", specified=" +amMemory
					+ ", max=" + maxMem);
			amMemory = maxMem;
		}

		// CPU 코어 개수 비교
		int maxVCores = appResponse.getMaximumResourceCapability().getVirtualCores();
		LOG.info("Max virtual cores capabililty of resource in this cluster " + maxVCores);
		if(amVCores > maxVCores)
		{
			LOG.info("AM virtual cores specified above max threshold of cluster. "
					+ "Using max value." + ", specified= " +amVCores
					+ ", max=" +maxVCores);
			amVCores = maxVCores;
		}

		// 애플리케이션 이름 설정
		ApplicationSubmissionContext appContext = app.getApplicationSubmissionContext();
		ApplicationId appId = appContext.getApplicationId();

		// 애플리케이션마스터용 시스템리 소스 설정
		appContext.setApplicationName(appName);
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(amMemory);
		capability.setVirtualCores(amVCores);
		appContext.setResource(capability);

		// 애플리케이션마스터 우선순위 설정
		Priority pri = Records.newRecord(Priority.class);
		pri.setPriority(amPriority);
		appContext.setPriority(pri);

		// 애플리케이션마스터용 큐 설정
		appContext.setQueue(amQueue);

		// 애플리케이션마스터용 ContainerLaunchContext 설정
		appContext.setAMContainerSpec(getAMContainerSpec(appId.getId()));

		// 애플리케이션 실행 요청
		LOG.info("Submitting application to ASM");
		yarnClient.submitApplication(appContext);

		// 애플리케이션 모니터링 수행
		return monitorApplication(appId);
	}

	private ContainerLaunchContext getAMContainerSpec(int appId) throws IOException, YarnException
	{
		// 애플리케이션마스터를 실행하는 컨테이너를 위한 컨텍스트 객체 생성
		ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);

		// 애플리케이션마스터용 LocalResource 맵
		Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();

		LOG.info("Copy App Master jar from local filesystem and add to local environment");

		// 애플리케이션마스터가 포함돼 있는 JAR 파일을 HDFS에 복사
		// 복사된 내용을 LocalResource에 추가
		FileSystem fs = FileSystem.get(conf);
		addToLocalResources(fs, appMasterJarPath, Constants.AM_JAR_NAME, appId, localResources, null);

		// 애플리케이션마스터용 컨텍스트에 LocalResource를 추가
		amContainer.setLocalResources(localResources);

		// 애플리케이션마스터용 시스템 환경변수를 설정
		LOG.info("Set the environment for the application master");
		amContainer.setEnvironment(getAMEnvironment(localResources, fs));

		// 애플리케이션마스터를 실행하기 위한 커맨드 라인 설정
		Vector<CharSequence> vargs = new Vector<CharSequence>(30);

		// 자바 실행 커맨드 라인 설정
		LOG.info("Setting up app master command");
		vargs.add(Environment.JAVA_HOME.$$() + "/bin/java");

		// 힙 메모리 설정
		vargs.add("-Xmx" + amMemory + "m");

		// 실행할 클래스 이름 설정
		vargs.add("com.hkl.hadoop.yarn.examples.MyApplicationMaster");
		
		//MyApplication에 전달할 파라미터 설정
		vargs.add("--container_memory " + String.valueOf(containerMemory));
		vargs.add("--container_vcores " + String.valueOf(containerVirtualCores));
		vargs.add("--num_containers " + String.valueOf(numContainers));
		vargs.add("--priority " + String.valueOf(requestPriority));

		// 로그 경로 설정
		vargs.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/AppMaster.stdout");
		vargs.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/AppMaster.stderr");

		// 설정된 커맨드 라인을 StringBuffer로 변환
		StringBuilder command = new StringBuilder();
		for(CharSequence str : vargs)
		{
			command.append(str).append(" ");
		}

		LOG.info("Completed setting up app master command " + command.toString());
		List<String> commands = new ArrayList<String>();
		commands.add(command.toString());
		amContainer.setCommands(commands);

		return amContainer;
	}

	private void addToLocalResources(FileSystem fs, String fileSrcPath, String fileDstPath,
			int appId, Map<String, LocalResource> localResources, String resources) throws IOException
	{
		String suffix = appName + "/" + appId + "/" + fileDstPath;
		Path dst = new Path(fs.getHomeDirectory(), suffix);

		// HDFS 파일 업로드
		if(fileSrcPath == null)
		{
			FSDataOutputStream ostream = null;

			try
			{
				ostream = FileSystem.create(fs, dst, new FsPermission((short)0710));
				ostream.writeUTF(resources);
			} 
			finally
			{
				IOUtils.closeQuietly(ostream);
			}
		}
		else 
		{
			fs.copyFromLocalFile(new Path(fileSrcPath), dst);
		}

		FileStatus scFileStatus = fs.getFileStatus(dst);

		// LocalResource를 생성해 HDFS에 업로드된 파일 정보를 수정
		LocalResource scRsrc = LocalResource.newInstance(ConverterUtils.getYarnUrlFromURI(dst.toUri()),
				LocalResourceType.FILE, LocalResourceVisibility.APPLICATION,
				scFileStatus.getLen(), scFileStatus.getModificationTime());
		localResources.put(fileDstPath, scRsrc);
	}

	private Map<String, String> getAMEnvironment(Map<String, LocalResource> localResources, FileSystem fs)
		throws IOException
	{
		Map<String, String> env = new HashMap<String, String>();

		// 애플리케이션마스터용 JAR 파일의 경로 설정
		LocalResource appJarResource = localResources.get(Constants.AM_JAR_NAME);
		Path hdfsAppJarPath = new Path(fs.getHomeDirectory(), appJarResource.getResource().getFile());
		FileStatus hdfsAppJarStatus = fs.getFileStatus(hdfsAppJarPath);
		long hdfsAppJarLength = hdfsAppJarStatus.getLen();
		long hdfsAppJarTimestamp = hdfsAppJarStatus.getModificationTime();

		env.put(Constants.AM_JAR_PATH, hdfsAppJarPath.toString());
		env.put(Constants.AM_JAR_TIMESTAMP, Long.toString(hdfsAppJarTimestamp));
		env.put(Constants.AM_JAR_LENGTH, Long.toString(hdfsAppJarLength));

		// 애플리케이션마스터가 JAR 파일의 경로를 클래스패스에 추가
		StringBuilder classPathEnv = new StringBuilder(Environment.CLASSPATH.$$())
			.append(ApplicationConstants.CLASS_PATH_SEPARATOR).append("./*");
		for(String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
					YarnConfiguration.DEFAULT_YARN_CROSS_PLATFORM_APPLICATION_CLASSPATH))
		{
			classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR);
			classPathEnv.append(c.trim());
		}
		
		env.put("CLASSPATH", classPathEnv.toString());
		return env;
	}

	private boolean monitorApplication(ApplicationId appId) throws YarnException, IOException
	{
		while(true)
		{
			// 1초간 쓰레드 대기 설정
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e)
			{
				LOG.error("Thread sleep in monitoring loop interrupted");
			}

			ApplicationReport report = yarnClient.getApplicationReport(appId);
			YarnApplicationState state = report.getYarnApplicationState();
			FinalApplicationStatus dsStatus = report.getFinalApplicationStatus();
			if(YarnApplicationState.FINISHED == state)
			{
				if(FinalApplicationStatus.SUCCEEDED == dsStatus)
				{
					LOG.info("Application has completed successfully. "
							+ "Breaking monitoring loop : Application:" + appId.getId());
					return true;
				}
				else
				{
					LOG.info("Application did finished unsuccessfully. "
							+ "YarnState=" + state.toString() + ", DSFinalStatus=" + dsStatus.toString()
							+ ". Breaking monitoring loop : ApplicationId:" + appId.getId());
					return false;
				}
			}
			else if(YarnApplicationState.KILLED == state || YarnApplicationState.FAILED == state)
			{
				LOG.info("Application did not finish."
						+ " YarnState=" + state.toString() + ", DSFinalStatus=" + dsStatus.toString()
						+ ". Breaking monitoring loop : ApplicationId:" + appId.getId());
				return false;
			}

			// 타임아웃이 발생할 경우 애플리케이션 강제로 종료
			if(System.currentTimeMillis() > (clientStartTime + clientTimeout))
			{
				LOG.info("Reached client specified timeout for application. Killing application"
						+ ". Breaking monitoring loop : ApplicationId:" + appId.getId());
				forceKillApplication(appId);
				return false;
			}

		}

	}
	

	private void forceKillApplication(ApplicationId appId) throws YarnException, IOException
	{
		yarnClient.killApplication(appId);
	}

	public static void main(String[] args)
	{
		boolean result = false;
		try
		{
			MyClient client = new MyClient();
			LOG.info("Initializing Client");
			try
			{
				boolean doRun = client.init(args);
				if(!doRun)
				{
					System.exit(0);
				}
			}
			catch(IllegalArgumentException e)
			{
				System.err.println(e.getLocalizedMessage());
				client.printUsage();
				System.exit(-1);
			}
			result = client.run();
		}
		catch(Throwable t)
		{
			LOG.fatal("Error running CLient", t);
			System.exit(1);
		}
		
		if(result)
		{
			LOG.info("Application completed successfully");
			System.exit(0);
		}
		LOG.error("Application failed to complete successfully");
		System.exit(2);
	}
}
