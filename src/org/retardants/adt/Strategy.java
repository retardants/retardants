package org.retardants.adt;

public enum Strategy {
    
    BATTLE_SHORTEST_EUCLIDEAN_ROUTE,
    BATTLE_DIJKSTRAS_TILE_PATH,
    
    FOOD_DIFFUSION_ONE_ANT_PER_FOOD, // one ant per food
    FOOD_DIFFUSION_ALL_ANTS, // all ants go uphill
    FOOD_SHORTEST_EUCLIDEAN_ROUTE, // one ant per food
    
    EXPLORATION_NEAREST_UNSEEN,
    EXPLORATION_LEAST_VISITED;

}
