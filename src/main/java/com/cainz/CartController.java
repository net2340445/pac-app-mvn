package com.cainz;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ExecuteOn(TaskExecutors.IO)
@Controller("/cart-point-ref")
public class CartController {
    private static final Logger LOG = LoggerFactory.getLogger(CartController.class);
    private final DynamoRepository dynamoRepository;

    public CartController(DynamoRepository dynamoRepository) {
        this.dynamoRepository = dynamoRepository;
    }

    @Post
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public String itemPost(@Body String body) throws JSONException {
        JSONObject response = new JSONObject();
        try {
            LOG.info("Request for the cart point reference:" + body);
            JSONObject json = new JSONObject(body);
            String memberCode = "";
            String pointAll = "";
            if (json.has("MEMBER_CODE")) {
                memberCode = json.getString("MEMBER_CODE");
            }
            if (json.has("POINT_ALL")) {
                pointAll = json.getString("POINT_ALL");
            }
            String rank = json.getString("MEMBER_RANK");
            Map<String, Object> map = new ObjectMapper().readValue(body, Map.class);
            Map<String, String> janCodes = new ObjectMapper().convertValue(map.get("JAN_CODE"), Map.class);
            List<String> janTrim = janCodes.values().stream().map(y -> {
                if (y.length() == 16) {
                    y = y.substring(3, 16);
                }
                return y;
            }).collect(Collectors.toList());
            List<String> validatedJanCodeList = janTrim.stream().filter(x -> (x.length() != 13)).collect(Collectors.toList());
            if (validatedJanCodeList.size() > 0) {
                JSONObject errorMessage = new JSONObject();
                errorMessage.put("Error code", "1001");
                errorMessage.put("Message", "入力されJANコード" + validatedJanCodeList.toString().replace("[", "").replace("]", "") + "は半角数字、13桁で入力してください。");
                LOG.warn(errorMessage.toString());
                if(validatedJanCodeList.equals(janCodes.values())) {
                    JSONObject memberInfoJsonObject = new JSONObject();
                    memberInfoJsonObject.put("MEMBER_RANK", rank);
                    response.put("MEMBER_INFO", memberInfoJsonObject);
                    response.put("RESULT_CODE", "0000");
                    response.put("RESULT_MESSAGE", "正常");
                    return response.toString();
                }
            }
            if (!memberCode.isBlank() && memberCode.length() > 13) {
                JSONObject errorMessage = new JSONObject();
                errorMessage.put("Error code", "1004");
                errorMessage.put("Message", "入力された会員コードは数字、13桁以内で入力してください。");
                LOG.warn(errorMessage.toString());
                JSONObject memberInfoJsonObject = new JSONObject();
                memberInfoJsonObject.put("MEMBER_RANK", rank);
                response.put("MEMBER_INFO", memberInfoJsonObject);
                response.put("RESULT_CODE", "0000");
                response.put("RESULT_MESSAGE", "正常");
                return response.toString();
            }
            if (!pointAll.isBlank() && pointAll.length() > 1) {
                JSONObject errorMessage = new JSONObject();
                errorMessage.put("Error code", "1005");
                errorMessage.put("Message", "入力されたポイントALL分は数字、1桁以内で入力してください。");
                LOG.warn(errorMessage.toString());
                JSONObject memberInfoJsonObject = new JSONObject();
                memberInfoJsonObject.put("MEMBER_RANK", rank);
                response.put("MEMBER_INFO", memberInfoJsonObject);
                response.put("RESULT_CODE", "0000");
                response.put("RESULT_MESSAGE", "正常");
                return response.toString();
            }
            if (rank.length() > 2) {
                JSONObject errorMessage = new JSONObject();
                errorMessage.put("Error code", "1003");
                errorMessage.put("Message", "入力された会員ランクは数字、2桁内で入力してください。");
                LOG.warn(errorMessage.toString());
                JSONObject memberInfoJsonObject = new JSONObject();
                memberInfoJsonObject.put("MEMBER_RANK", rank);
                response.put("MEMBER_INFO", memberInfoJsonObject);
                response.put("RESULT_CODE", "0000");
                response.put("RESULT_MESSAGE", "正常");
                return response.toString();
            }
            String rankString;
            switch (rank) {
                case "0":
                    rankString = "100";
                    break;
                case "1":
                    rankString = "200";
                    break;
                case "2":
                    rankString = "400";
                    break;
                case "3":
                    rankString = "800";
                    break;
                case "4":
                    rankString = "1600";
                    break;
                case "99":
                    rankString = "00";
                    break;
                default:
                    JSONObject memberInfoJsonObject = new JSONObject();
                    memberInfoJsonObject.put("MEMBER_RANK", rank);
                    response.put("MEMBER_INFO", memberInfoJsonObject);
                    response.put("RESULT_CODE", "0000");
                    response.put("RESULT_MESSAGE", "正常");
                    return response.toString();
            }
            Map<String, String> storeCodesListMap = new ObjectMapper().convertValue(map.get("STORE_CODE"), Map.class);
            if (storeCodesListMap.entrySet().isEmpty()) {
                JSONObject errorMessage = new JSONObject();
                errorMessage.put("Error code", "1002");
                errorMessage.put("Message", "入力された店舗コード＄｛店舗コード｝は半角数字、4桁以内で入力してください。");
                LOG.warn(errorMessage.toString());
                JSONObject memberInfoJsonObject = new JSONObject();
                memberInfoJsonObject.put("MEMBER_RANK", rank);
                response.put("MEMBER_INFO", memberInfoJsonObject);
                response.put("RESULT_CODE", "0000");
                response.put("RESULT_MESSAGE", "正常");
                return response.toString();
            }
            List<String> stringList = storeCodeReplacement(storeCodesListMap);
            List<String> validatedStoreCodeList = stringList.stream().filter(x -> (x.length() != 4)).collect(Collectors.toList());
            if (validatedStoreCodeList.size() > 0) {
                JSONObject errorMessage = new JSONObject();
                errorMessage.put("Error code", "1002");
                errorMessage.put("Message", "入力された店舗コード" + validatedStoreCodeList.toString().replace("[", "").replace("]", "") + "は半角数字、4桁以内で入力してください。");
                LOG.warn(errorMessage.toString());
                JSONObject memberInfoJsonObject = new JSONObject();
                memberInfoJsonObject.put("MEMBER_RANK", rank);
                response.put("MEMBER_INFO", memberInfoJsonObject);
                response.put("RESULT_CODE", "0000");
                response.put("RESULT_MESSAGE", "正常");
                return response.toString();
            }
            for(String jan : validatedJanCodeList) {
                janCodes.values().remove(jan);
            }
            List<String> janCodeList = new ArrayList<>(janCodes.values());
            List<String> storeCodesList = new ArrayList<>(stringList);
            List<Map<String, AttributeValue>> itemList = new ArrayList<>();
            Map<String, String> expressionAttributesNames = new HashMap<>();
            expressionAttributesNames.put("#jan", "jan");
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            for (String janCode : janCodeList) {
                expressionAttributeValues.put(":janValue", AttributeValue.builder().s(janCode).build());
                LOG.info("fetching record for JanCode :" + janCode);
                QueryResponse queryResponse = dynamoRepository.queryIndex(expressionAttributesNames,expressionAttributeValues);
                itemList.addAll(queryResponse.items());
            }
            Map<Object, Map<Object, List<Map<String, AttributeValue>>>> sortedList = filteringMultipleFactor(itemList, storeCodesList, rankString);
            response = responseJsonObject(sortedList, rank);
            LOG.info("Response for the CART POINT reference:" + response.toString());
            if (response.length() == 0) {
                JSONObject memberInfoJsonObject = new JSONObject();
                memberInfoJsonObject.put("MEMBER_RANK", rank);
                response.put("MEMBER_INFO", memberInfoJsonObject);
                response.put("RESULT_CODE", "0000");
                response.put("RESULT_MESSAGE", "正常");
            }
            return response.toString();
        } catch (Exception e) {
            LOG.error("A system error occurred " + e.getMessage());
            e.printStackTrace();
            response.put("Error Code", "1000");
            response.put("Message", "システムエラーが発生しました」");
            return response.toString();
        }
    }

    public List<String> storeCodeReplacement(Map<String, String> storeCodesListMap) {
        return storeCodesListMap.values().stream().map(x -> {
            if (x.equals("830")) {
                x = "0328";
            }
            if (x.equals("837")) {
                x = "0329";
            }
            if (x.equals("838")) {
                x = "0316";
            }
            if (x.equals("871")) {
                x = "0323";
            }
            if (x.length() == 3) {
                x = String.format("%04d", Integer.parseInt(x));
            }
            if (x.length() == 2) {
                x = String.format("%04d", Integer.parseInt(x));
            }
            if (x.length() == 1) {
                x = String.format("%04d", Integer.parseInt(x));
            } else {
                return x;
            }
            return x;
        }).collect(Collectors.toList());
    }

    static void CombinationPossible(Map<Integer, List<Integer>> map, int Input_Array[], int Empty_Array[], int Start_Element, int End_Element, int Array_Index, int r) {
        if (Array_Index == r) {
            int sum = 0;
            List<Integer> list = new ArrayList<>();
            for (int x = 0; x < r; x++) {
                sum += Empty_Array[x];
                list.add(Empty_Array[x]);
            }
            map.put(sum, list);
            return;
        }
        for (int y = Start_Element; y <= End_Element && End_Element - y + 1 >= r - Array_Index; y++) {
            Empty_Array[Array_Index] = Input_Array[y];
            CombinationPossible(map, Input_Array, Empty_Array, y + 1, End_Element, Array_Index + 1, r);
        }
    }

    static void Print_Combination(Map<Integer, List<Integer>> map, int[] Input_Arrary, int n, int r) {
        int Empty_Array[] = new int[r];
        CombinationPossible(map, Input_Arrary, Empty_Array, 0, n - 1, 0, r);
    }

    public Map<Object, Map<Object, List<Map<String, AttributeValue>>>> filteringMultipleFactor(List<Map<String, AttributeValue>> listToBeSorted, List<String> storeCodesList, String rank) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        int Input_Array[] = {100, 200, 400, 800, 1600};
        int[] r = {1, 2, 3, 4, 5};
        int n = Input_Array.length;
        for (int i = 0; i < 5; i++) {
            Print_Combination(map, Input_Array, n, r[i]);
        }
        ZoneId zoneId = ZoneId.of("Asia/Tokyo");
        LocalDateTime now = LocalDateTime.now(zoneId);
        String currentTime = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        List<Map<String, AttributeValue>> filteredByStoreCodes = listToBeSorted.stream().filter(storeCode -> storeCodesList.contains(storeCode.get("pk").s().substring(0, 4))).collect(Collectors.toList());
        LOG.info("Item List with size: " + filteredByStoreCodes.size() + "with items :" + filteredByStoreCodes);
        List<Map<String, AttributeValue>> filteredList = filteredByStoreCodes.stream().filter(itemRank ->
                {
                    int itemRank1 = Integer.parseInt(itemRank.get("rank").s());
                    if (map.get(itemRank1) != null) {
                        return map.get(itemRank1).contains(Integer.parseInt(rank));
                    } else return false;
                }
        ).collect(Collectors.toList());
        LOG.info("filtered List with size: " + filteredList.size() + "with items :" + filteredList);
        long currentDateTime = Long.parseLong(currentTime);
        List<Map<String, AttributeValue>> filteredByDate = filteredList.stream().filter(itemDate ->
                {
                    long endDate = Long.parseLong(itemDate.get("edt").s());
                    long sdtDate = Long.parseLong(itemDate.get("sdt").s());
                    return currentDateTime >= sdtDate && endDate >= currentDateTime;
                }
        ).collect(Collectors.toList());
        LOG.info(" FilteredByDateList with size: " + filteredByDate.size() + "with items :" + filteredByDate);
        Map<Object, Map<Object, List<Map<String, AttributeValue>>>> sortList = filteredByDate.stream().collect(Collectors.groupingBy(itemPk -> itemPk.get("pk").s().substring(5, 14), Collectors.groupingBy(itemDesc -> itemDesc.get("promotionDesc").s())));
        LOG.info("Sorted List with size: " + sortList.size() + "with items :" + sortList);
        return sortList;
    }

    public JSONObject responseJsonObject(Map<Object, Map<Object, List<Map<String, AttributeValue>>>> sortedList, String rank) throws JSONException {
        JSONObject responseMethod = new JSONObject();
        JSONArray promotionJsonArray = new JSONArray();
        JSONObject memberInfoJsonObject = new JSONObject();
        for (Map.Entry<Object, Map<Object, List<Map<String, AttributeValue>>>> promotionSorted : sortedList.entrySet()) {
            for (Map.Entry<Object, List<Map<String, AttributeValue>>> descSorted : promotionSorted.getValue().entrySet()) {
                JSONObject promotionJsonObject = new JSONObject();
                JSONArray iTermJsonList = new JSONArray();
                promotionJsonObject.put("PROMOTION_CODE", promotionSorted.getKey());
                promotionJsonObject.put("PROMOTION_DESC", descSorted.getKey());
                for (Map<String, AttributeValue> item : descSorted.getValue()) {
                    JSONObject iTermJsonObject = new JSONObject();
                    promotionJsonObject.put("PROMOTION_EDT",item.get("edt").s());
                    iTermJsonObject.put("JAN_CODE:", item.get("jan").s());
                    iTermJsonObject.put("POINT_PLUS", item.get("point").s());
                    if (item.get("pk").s().substring(0, 4).startsWith("0328")) {
                        iTermJsonObject.put("STORE_CODE", item.get("pk").s().replace("0328", "830").substring(0, 3));
                    } else if (item.get("pk").s().substring(0, 4).startsWith("0329")) {
                        iTermJsonObject.put("STORE_CODE", item.get("pk").s().replace("0329", "837").substring(0, 3));
                    } else if (item.get("pk").s().substring(0, 4).startsWith("0316")) {
                        iTermJsonObject.put("STORE_CODE", item.get("pk").s().replace("0316", "838").substring(0, 3));
                    } else if (item.get("pk").s().substring(0, 4).startsWith("0323")) {
                        iTermJsonObject.put("STORE_CODE", item.get("pk").s().replace("0323", "871").substring(0, 3));
                    } else if (item.get("pk").s().substring(0, 4).startsWith("000")) {
                        iTermJsonObject.put("STORE_CODE", item.get("pk").s().substring(3, 4));
                    } else if (item.get("pk").s().substring(0, 4).startsWith("00")) {
                        iTermJsonObject.put("STORE_CODE", item.get("pk").s().substring(2, 4));
                    } else if (item.get("pk").s().substring(0, 4).startsWith("0")) {
                        iTermJsonObject.put("STORE_CODE", item.get("pk").s().substring(1, 4));
                    } else {
                        iTermJsonObject.put("STORE_CODE", item.get("pk").s().substring(0, 4));
                    }
                    iTermJsonList.put(iTermJsonObject);
                }
                promotionJsonObject.put("ITERM_INFO", iTermJsonList);
                promotionJsonArray.put(promotionJsonObject);
            }
            memberInfoJsonObject.put("MEMBER_RANK", rank);
            memberInfoJsonObject.put("PROMOTION_INFO", promotionJsonArray);
            responseMethod.put("RESULT_CODE", "0000");
            responseMethod.put("RESULT_MESSAGE", "正常");
            responseMethod.put("MEMBER_INFO", memberInfoJsonObject);
        }
        return responseMethod;
    }
}
