package ftt.data;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для предоставления данных графика по запросу
 */
public interface ChartDataProvider {
    /**
     * Получить данные графика в виде списка точек
     * Каждая точка - это Map, где ключ - имя параметра, значение - данные
     * @return список точек данных графика
     */
    List<Map<String, Object>> getChartData();
    
    /**
     * Получить метаданные графика (названия осей, единицы измерения и т.д.)
     * @return Map с метаданными
     */
    Map<String, String> getChartMetadata();
}