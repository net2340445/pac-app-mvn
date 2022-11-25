package com.cainz;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Requires(condition = CIAwsRegionProviderChainCondition.class)
@Requires(condition = CIAwsCredentialsProviderChainCondition.class)
@Requires(beans = {DynamoConfiguration.class, DynamoDbClient.class})
@Singleton
@Primary
public class DynamoRepository {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoRepository.class);

    protected final DynamoDbClient dynamoDbClient;
    protected final DynamoConfiguration dynamoConfiguration;

    public DynamoRepository(DynamoDbClient dynamoDbClient,
                            DynamoConfiguration dynamoConfiguration) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoConfiguration = dynamoConfiguration;
    }

    @EventListener
    @Async
    public void dummyDatabaseCall(final Object event) {
        LOG.info("loading at startup");
        String dummyJanCodeList = "0000000000000";
        Map<String, String> expressionAttributesNames = new HashMap<>();
        expressionAttributesNames.put("#jan", "jan");
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":janValue", AttributeValue.builder().s(dummyJanCodeList).build());
        LOG.info("fetching record for JanCode :" + dummyJanCodeList);
        QueryResponse queryResponse = queryIndex(expressionAttributesNames, expressionAttributeValues);
    }

    public int getSomething() {
        DescribeTableResponse response = dynamoDbClient.describeTable(DescribeTableRequest.builder()
                .tableName(dynamoConfiguration.getTableName())
                .build());

        String abc = response.table().keySchema().toArray().toString();
        LOG.debug(abc);
        listAllTables();
        System.out.println("getSomething Success!!!");
        System.out.println(abc);
        return dynamoDbClient.listTables().tableNames().size();
    }

    public boolean existsTable() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(dynamoConfiguration.getTableName())
                    .build());
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    public String createTableComKey(String tableName) {
        CreateTableRequest request = CreateTableRequest.builder()
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("Language")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("Greeting")
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("Language")
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("Greeting")
                                .keyType(KeyType.RANGE)
                                .build())
                .provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(new Long(10))
                                .writeCapacityUnits(new Long(10)).build())
                .tableName(tableName)
                .build();

        String tableId = "";

        try {
            CreateTableResponse result = dynamoDbClient.createTable(request);
            tableId = result.tableDescription().tableId();
            return tableId;
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }


    // Only one PK  no SK
    public String getDynamoDBItem(String key, String keyVal) {

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(key, AttributeValue.builder()
                .s(keyVal)
                .build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(dynamoConfiguration.getTableName())
                .build();

        try {
            Map<String, AttributeValue> returnedItem = dynamoDbClient.getItem(request).item();
            if (returnedItem != null) {
                Set<String> keys = returnedItem.keySet();
                System.out.println("Amazon DynamoDB table attributes: \n");

                for (String key1 : keys) {
                    System.out.format("%s: %s\n", key1, returnedItem.get(key1).toString());
                }
            } else {
                System.out.format("No item found with the key %s!\n", key);
            }
            LOG.debug("found with the key");
            System.out.println("getItem Success!!!");
        } catch (DynamoDbException e) {
            System.out.println("getItem Failed!!!");
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return "found with the key";
    }

    // Only one PK  no SK
    public String batchGetItem() {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("pk", AttributeValue.builder().s("88881P2222R33").build());
//        item.put("sk", AttributeValue.builder().s("1").build());
        PutItemRequest putItemRequest = PutItemRequest.builder().tableName(dynamoConfiguration.getTableName()).item(item).returnValues(ReturnValue.ALL_OLD.toString()).build();
        dynamoDbClient.putItem(putItemRequest);
        Map<String, KeysAndAttributes> items = new HashMap<String, KeysAndAttributes>();
        // Put a valid key and a null one
        items.put(dynamoConfiguration.getTableName(),
                KeysAndAttributes.builder().keys(mapKey("pk", AttributeValue.builder().s("88881P2222R33").build()), null).build());

        BatchGetItemRequest request = BatchGetItemRequest.builder().requestItems(items).build();
        try {
            BatchGetItemResponse response = dynamoDbClient.batchGetItem(request);
            System.out.println("BatchGetItem Success!!!  ,The responses size is "+response.responses().size());
            Map<String, List<Map<String, AttributeValue>>> resultMap=response.responses();
            for (Map.Entry<String,List<Map<String,AttributeValue>>> theHouse :resultMap.entrySet()){
                List<Map<String, AttributeValue>> dd = theHouse.getValue();
                System.out.println("dd size: "+ dd.size());
                StringBuilder strs=new StringBuilder();
                for (int i = 0; i < dd.size(); i++) {
                    Map<String, AttributeValue> ee = dd.get(i);
                    Iterator<String> iterator = ee.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        AttributeValue cc = ee.get(key);
                        strs.append(cc.toString());
                    }
                }
                System.out.println("dd strs"+strs.toString());
            }
        } catch (AwsServiceException exception) {
            System.out.println("BatchGetItem failed!!!");
            System.out.println(exception.getMessage());
        }
        return "this ok";
    }

    /**
     * Gets a map of key values for the single hash key attribute value given.
     */
    protected Map<String, AttributeValue> mapKey(String attributeName, AttributeValue value) {
        HashMap<String, AttributeValue> map = new HashMap<String, AttributeValue>();
        map.put(attributeName, value);
        return map;
    }

    public void listAllTables() {

        boolean moreTables = true;
        String lastName = null;

        while (moreTables) {
            try {
                ListTablesResponse response = null;
                if (lastName == null) {
                    ListTablesRequest request = ListTablesRequest.builder().build();
                    response = dynamoDbClient.listTables(request);
                } else {
                    ListTablesRequest request = ListTablesRequest.builder()
                            .exclusiveStartTableName(lastName).build();
                    response = dynamoDbClient.listTables(request);
                }

                List<String> tableNames = response.tableNames();

                if (tableNames.size() > 0) {
                    for (String curName : tableNames) {
                        System.out.format("* %s\n", curName);
                    }
                } else {
                    System.out.println("No tables found!");
                    System.exit(0);
                }

                lastName = response.lastEvaluatedTableName();
                if (lastName == null) {
                    moreTables = false;
                }
            } catch (DynamoDbException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
        System.out.println("\nDone!");
    }

    public QueryResponse queryIndex(Map<String, String> expressionAttributesNames ,Map<String, AttributeValue> expressionAttributeValues) {
        QueryResponse response = null;
        try {
            QueryRequest request = QueryRequest.builder()
                    .tableName(dynamoConfiguration.getTableName())
                    .indexName("jan-index")
                    .keyConditionExpression("#jan = :janValue")
                    .expressionAttributeNames(expressionAttributesNames)
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();
            response = dynamoDbClient.query(request);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return response;
    }
}

