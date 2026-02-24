package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.PropertyPost;
import com.dojangkok.backend.dto.propertypost.PropertyPostSearchRequestDto;

import java.util.List;

public interface PropertyPostSearchRepository {

    List<PropertyPost> search(PropertyPostSearchRequestDto searchRequest, int pageSize);
}
