package org.example.ilpcw1.dto;

import java.util.List;

public class QueryDTO {
    private List<QueryAttributeDTO> queries;

    public List<QueryAttributeDTO> getQueries() { return queries; }
    public void setQueries(List<QueryAttributeDTO> queries) { this.queries = queries; }
}