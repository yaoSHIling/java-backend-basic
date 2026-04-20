package com.example.basic.modules.workflow.engine;

import com.example.basic.modules.workflow.engine.WfGraph;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 图工具（工作流引擎专用）
 *
 * <p>提供：
 * <ul>
 *   <li>DAG 检测：防止循环依赖</li>
 *   <li>拓扑排序：从 start 节点按依赖顺序排列所有节点</li>
 *   <li>可达性分析：从某个节点能到达哪些节点</li>
 *   <li>前驱/后继查找</li>
 *   <li>入度/出度计算</li>
 * </ul>
 */
public class GraphUtil {

    /**
     * 检测图中是否存在环（判断是否为有效 DAG）
     *
     * @return true = 有环（无效），false = 无环（有效）
     */
    public static boolean hasCycle(WfGraph graph) {
        if (graph == null || graph.getNodes() == null) return false;

        Map<String, Integer> inDegree = buildInDegreeMap(graph);
        Queue<String> queue = new ArrayDeque<>();

        // 入度为 0 的节点入队
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) queue.offer(e.getKey());
        }

        int visited = 0;
        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            visited++;

            // 找该节点的出边
            for (WfGraph.Edge edge : graph.getEdges()) {
                if (nodeId.equals(edge.getSource())) {
                    String target = edge.getTarget();
                    int newDegree = inDegree.get(target) - 1;
                    inDegree.put(target, newDegree);
                    if (newDegree == 0) queue.offer(target);
                }
            }
        }

        return visited != graph.getNodes().size();
    }

    /**
     * 拓扑排序（Kahn 算法）
     * 返回从 start 节点开始的节点执行顺序。
     * 如果有环，返回 null。
     */
    public static List<String> topologicalSort(WfGraph graph) {
        if (hasCycle(graph)) return null;

        Map<String, Integer> inDegree = buildInDegreeMap(graph);
        List<String> result = new ArrayList<>();
        Queue<String> queue = new ArrayDeque<>();

        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) queue.offer(e.getKey());
        }

        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            result.add(nodeId);

            for (WfGraph.Edge edge : graph.getEdges()) {
                if (nodeId.equals(edge.getSource())) {
                    String target = edge.getTarget();
                    int newDegree = inDegree.get(target) - 1;
                    inDegree.put(target, newDegree);
                    if (newDegree == 0) queue.offer(target);
                }
            }
        }

        return result;
    }

    /**
     * 从指定节点出发，找到所有可达节点
     */
    public static Set<String> reachableFrom(WfGraph graph, String startNodeId) {
        Set<String> visited = new LinkedHashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.offer(startNodeId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            for (WfGraph.Edge edge : graph.getEdges()) {
                if (current.equals(edge.getSource())) {
                    queue.offer(edge.getTarget());
                }
            }
        }

        return visited;
    }

    /**
     * 找到指定节点的所有直接后继节点
     */
    public static List<String> getSuccessors(WfGraph graph, String nodeId) {
        return graph.getEdges().stream()
            .filter(e -> nodeId.equals(e.getSource()))
            .map(WfGraph.Edge::getTarget)
            .collect(Collectors.toList());
    }

    /**
     * 找到指定节点的所有直接前驱节点
     */
    public static List<String> getPredecessors(WfGraph graph, String nodeId) {
        return graph.getEdges().stream()
            .filter(e -> nodeId.equals(e.getTarget()))
            .map(WfGraph.Edge::getSource)
            .collect(Collectors.toList());
    }

    /**
     * 找到 start 类型的节点
     */
    public static WfGraph.Node findStartNode(WfGraph graph) {
        return graph.getNodes() == null ? null
            : graph.getNodes().stream()
                .filter(n -> "start".equals(n.getType()))
                .findFirst().orElse(null);
    }

    /**
     * 根据节点 ID 查找节点
     */
    public static WfGraph.Node findNode(WfGraph graph, String nodeId) {
        if (graph == null || graph.getNodes() == null) return null;
        return graph.getNodes().stream()
            .filter(n -> nodeId.equals(n.getId()))
            .findFirst().orElse(null);
    }

    /**
     * 根据边ID查找边
     */
    public static WfGraph.Edge findEdge(WfGraph graph, String edgeId) {
        if (graph == null || graph.getEdges() == null) return null;
        return graph.getEdges().stream()
            .filter(e -> edgeId.equals(e.getId()))
            .findFirst().orElse(null);
    }

    // ==================== 内部工具 ====================

    private static Map<String, Integer> buildInDegreeMap(WfGraph graph) {
        Map<String, Integer> inDegree = new HashMap<>();

        // 初始化所有节点入度为 0
        if (graph.getNodes() != null) {
            for (WfGraph.Node node : graph.getNodes()) {
                inDegree.put(node.getId(), 0);
            }
        }

        // 遍历边，累加入度
        if (graph.getEdges() != null) {
            for (WfGraph.Edge edge : graph.getEdges()) {
                inDegree.merge(edge.getTarget(), 1, Integer::sum);
            }
        }

        return inDegree;
    }

    /**
     * 找到一条从 source 到 target 的路径（DFS）
     */
    public static boolean hasPath(WfGraph graph, String source, String target) {
        return reachableFrom(graph, source).contains(target);
    }

    /**
     * 统计每个节点的入度和出度
     */
    public static Map<String, NodeDegree> computeDegrees(WfGraph graph) {
        Map<String, NodeDegree> result = new HashMap<>();

        if (graph.getNodes() != null) {
            for (WfGraph.Node node : graph.getNodes()) {
                result.put(node.getId(), new NodeDegree());
            }
        }

        if (graph.getEdges() != null) {
            for (WfGraph.Edge edge : graph.getEdges()) {
                result.computeIfAbsent(edge.getSource(), k -> new NodeDegree()).outDegree++;
                result.computeIfAbsent(edge.getTarget(), k -> new NodeDegree()).inDegree++;
            }
        }

        return result;
    }

    @Data
    public static class NodeDegree {
        public int inDegree = 0;
        public int outDegree = 0;
    }
}
