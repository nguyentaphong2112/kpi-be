package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.request.CardObjectRequest;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.CardObjectResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

public interface CardObjectService {
    TableResponseEntity<CardObjectResponse.SearchResult> searchData(CardObjectRequest.SearchForm dto);

    ResponseEntity<Object> exportData(CardObjectRequest.SearchForm dto) throws Exception;

    List<CardObjectResponse.DetailBean> getListObject(String objType, Long objId);

    ResponseEntity<Object> exportCard(PartnersRequest.PrintCard dto) throws Exception;
}
