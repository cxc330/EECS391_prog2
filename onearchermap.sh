javac -cp 'lib/SimpleRTSv3.1.jar' src/edu/cwru/SimpleRTS/agent/ABAgent.java

java -cp lib/SimpleRTSv3.1.jar:src/edu/cwru/SimpleRTS/agent:src edu.cwru.SimpleRTS.Main --config data/proj2Config.xml data/game_2fv1a.map --agent  edu.cwru.SimpleRTS.agent.ABAgent 0 --agentparam 3 --agent AdversaryAgent 1 --agent edu.cwru.SimpleRTS.agent.visual.VisualAgent 0 --agentparam true --agentparam true

