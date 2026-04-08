package com.jad;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jad.connector.DBConnector;
import com.jad.dto.MachineScheduleDTO;
import com.jad.planner.ManufacturePlanner;
import com.jad.service.MachineToolService;
import com.jad.service.OperationTypeService;
import com.jad.service.ProductRecipeService;
import com.jad.service.ProductService;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        MachineToolService machineToolService = new MachineToolService(DBConnector.getInstance());
        OperationTypeService operationTypeService = new OperationTypeService(DBConnector.getInstance());
        ProductService productService = new ProductService(DBConnector.getInstance());
        ProductRecipeService productRecipeService = new ProductRecipeService(DBConnector.getInstance());

        ManufacturePlanner planner = new ManufacturePlanner(
                productService,
                productRecipeService,
                operationTypeService,
                machineToolService
        );

        int targetProductId = 11467;
        double targetQuantity = 500.0;

        List<MachineScheduleDTO> finalPlan = planner.generatePlan(targetProductId, targetQuantity);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(finalPlan);

        System.out.println(jsonOutput);

        try (FileWriter writer = new FileWriter("plan_fabrication.json")) {
            writer.write(jsonOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DBConnector.getInstance().disconnect();
    }
}