package com.profiler;

import java.util.Map.Entry;
import java.util.logging.Logger;

import com.profiler.sender.AgentInfoSender;
import com.profiler.thread.CurrentJVMStateCheckThread;

public class Agent {

	public static final String FQCN = Agent.class.getName();

	private static final Logger logger = Logger.getLogger(Agent.class.getName());

	private volatile boolean alive = false;

	private final ServerInfo serverInfo;

	private int agentHash = 0;

	private Agent() {
		this.serverInfo = new ServerInfo();
	}

	private static class SingletonHolder {
		public static final Agent INSTANCE = new Agent();
	}

	public static Agent getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setIsAlive(boolean alive) {
		this.alive = alive;
	}

	public ServerInfo getServerInfo() {
		return this.serverInfo;
	}

	/**
	 * Agent hash code.
	 * @return
	 */
	public int getAgentHashCode() {
		//TODO: string format으로 변경, DTO때문에 일단 int로 구현함.
		//TODO: hashcode 생성 방법도 변경해야함.
		if (agentHash == 0) {
			String portNumbers = "";
			for (Entry<Integer, String> entry : serverInfo.getConnectors().entrySet()) {
				portNumbers += " " + entry.getKey();
			}
			portNumbers = portNumbers.trim();
			agentHash = (serverInfo.getHostip() + portNumbers).hashCode();
		}

		return agentHash;
	}

	/**
	 * HIPPO 서버로 WAS정보를 전송한다.
	 */
	//TODO: life cycle을 체크하는 방법으로 바꿀까..  DEAD, STARTING, STARTED, STOPPING, STOPPED
	public void sendStartupInfo() {
		logger.info("Send startup information to HIPPO server.");

		AgentInfoSender sender = new AgentInfoSender(true);
		sender.start();
	}

	public void start() {
		logger.info("Starting HIPPO Agent.");

		CurrentJVMStateCheckThread tracer = new CurrentJVMStateCheckThread();
		tracer.setName("HIPPO-JVMStateCheck");
		tracer.setDaemon(true);
		tracer.start();
	}

	public void stop() {
		logger.info("Stopping HIPPO Agent.");

		AgentInfoSender sender = new AgentInfoSender(false);
		sender.start();
	}

	public static void startAgent() {
		Agent.getInstance().start();
	}

	public static void stopAgent() throws Exception {
		Agent.getInstance().stop();
	}
}
