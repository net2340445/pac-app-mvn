package com.cainz;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

@Controller
public class HomeController {
    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
    private final DynamoRepository DynamoRepository;

    public HomeController(com.cainz.DynamoRepository dynamoRepository) {
        DynamoRepository = dynamoRepository;
    }

    @Get
    public Map<String, Object> index() {
        LOG.info("test for log info");
        LOG.warn("test for log debug");
        boolean mark = DynamoRepository.existsTable();
        DynamoRepository.listAllTables();
        return Collections.singletonMap("message", "Hello World  - DynamoDB is " + mark);
    }

    @Post
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public String homePost(@Body String body) {
        boolean mark = DynamoRepository.existsTable();
        DynamoRepository.listAllTables();
        return body + "-------------DynamoDB is " + mark;
    }

}
