package org.example.ilpcw1.services;

import org.example.ilpcw1.dto.NoFlyZoneDTO;
import org.example.ilpcw1.model.LngLat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PathService {

    private static final Logger log = LoggerFactory.getLogger(PathService.class);
    private static final double STEP_SIZE = 0.00015;

    public List<LngLat> findPath(LngLat start, LngLat goal, List<NoFlyZoneDTO> noFlyZones, double maxMoves) {
        log.info("Starting path finding from {} to {} with maxMoves {}", start, goal, maxMoves);
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Set<String> closedSet = new HashSet<>();
        Map<String, Node> openSetMap = new HashMap<>();
        Map<String, Node> cameFrom = new HashMap<>();

        Node startNode = new Node(start, 0, heuristic(start, goal));
        openSet.add(startNode);
        openSetMap.put(key(start), startNode);
        int iterations = 0;
        int maxIterations = 10000000;

        while (!openSet.isEmpty() && iterations < maxIterations) {
            iterations++;

            if (iterations % 10000 == 0) {
                log.info("Iteration: {}", iterations);
            }

            Node current = openSet.poll();
            String currentKey = key(current.position);
            openSetMap.remove(currentKey);

            if (closedSet.contains(currentKey)) continue;
            closedSet.add(currentKey);

//            if (iterations % 1000 == 0) {
//                log.info("Iteration {}: Current pos: {}, Distance to goal: {}, G-score: {}, OpenSet size: {}",
//                        iterations, current.position, distance(current.position, goal), current.g, openSet.size());
//            }

            // Check if goal reached
            if (distance(current.position, goal) < STEP_SIZE) {
                log.info("Goal reached after {} iterations with path length {}", iterations, current.g);
                return reconstructPath(cameFrom, current);
            }

            // Don't explore further if we've exceeded maxMoves
            if (current.g >= maxMoves) {
                continue;
            }

            // Generate neighbors (8 directions)
            for (int i = 0; i < 8; i++) {
                double angle = i * 45;
                LngLat neighbor = calculateNextPosition(current.position.getLng(), current.position.getLat(), angle);

                if (isInNoFlyZone(neighbor, noFlyZones)) {
                    continue;
                }

                String nKey = key(neighbor);
                if (closedSet.contains(nKey)) {
                    continue;
                }

                double tentativeG = current.g + 1;

                // check to prune distant nodes
                double distanceToGoal = distance(neighbor, goal);
                double estimatedMovesNeeded = distanceToGoal / STEP_SIZE;
                if (tentativeG + estimatedMovesNeeded > maxMoves * 1.5) {
                    continue; // Skip nodes that are likely too far
                }

                // Check if this path is better than existing
                Node existingNode = openSetMap.get(nKey);
                if (existingNode == null || tentativeG < existingNode.g) {
                    Node neighborNode = new Node(neighbor, tentativeG, heuristic(neighbor, goal));
                    cameFrom.put(nKey, current);

                    if (existingNode != null) {
                        openSet.remove(existingNode);
                    }
                    openSet.add(neighborNode);
                    openSetMap.put(nKey, neighborNode);
                }

            }
        }

        log.warn("No path found after {} iterations (maxIterations: {})", iterations, maxIterations);
        return Collections.emptyList();
    }

    private double heuristic(LngLat a, LngLat b) {
        return Math.sqrt(Math.pow(a.getLng() - b.getLng(), 2) + Math.pow(a.getLat() - b.getLat(), 2));
    }

    private double distance(LngLat a, LngLat b) {
        return Math.sqrt(Math.pow(a.getLng() - b.getLng(), 2) + Math.pow(a.getLat() - b.getLat(), 2));
    }

    private LngLat calculateNextPosition(double lng, double lat, double angle) {
        double rad = Math.toRadians(angle);
        double newLng = lng + STEP_SIZE * Math.cos(rad);
        double newLat = lat + STEP_SIZE * Math.sin(rad);
        return new LngLat(newLng, newLat);
    }

    public boolean isInNoFlyZone(LngLat pos, List<NoFlyZoneDTO> noFlyZones) {
        for (NoFlyZoneDTO zone : noFlyZones) {
            List<LngLat> vertices = zone.getVertices().stream()
                    .map(v -> new LngLat(v.getLng(), v.getLat()))
                    .collect(Collectors.toList());
            if (isPointInPolygon(pos, vertices)) return true;
        }
        return false;
    }

    private boolean isPointInPolygon(LngLat pos, List<LngLat> vertices) {
        int n = vertices.size();
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            if (((vertices.get(i).getLat() > pos.getLat()) != (vertices.get(j).getLat() > pos.getLat())) &&
                    (pos.getLng() < (vertices.get(j).getLng() - vertices.get(i).getLng()) * (pos.getLat() - vertices.get(i).getLat()) / (vertices.get(j).getLat() - vertices.get(i).getLat()) + vertices.get(i).getLng())) {
                inside = !inside;
            }
        }
        return inside;
    }

    private List<LngLat> reconstructPath(Map<String, Node> cameFrom, Node current) {
        List<LngLat> path = new ArrayList<>();
        while (current != null) {
            path.add(0, current.position);
            current = cameFrom.get(key(current.position));
        }
        return path;
    }

    private String key(LngLat pos) {
        double precision = 1e4;
        long lngRounded = Math.round(pos.getLng() * precision);
        long latRounded = Math.round(pos.getLat() * precision);
        return lngRounded + "," + latRounded;
    }

    private static class Node {
        LngLat position;
        double g, h, f;

        Node(LngLat position, double g, double h) {
            this.position = position;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }
}