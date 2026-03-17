package com.jad;

import com.jad.connector.DBConnector;
import com.jad.service.MachineToolService;
import com.jad.service.OperationTypeService;
import com.jad.service.ProductRecipeService;
import com.jad.service.ProductService;

import java.sql.SQLException;
// Branche Elliott

public class Main {
    public static void main(String[] args) throws SQLException {
        MachineToolService machineToolService = new MachineToolService(DBConnector.getInstance());
        machineToolService.getAll().forEach(System.out::println);

        OperationTypeService operationTypeService = new OperationTypeService(DBConnector.getInstance());
        operationTypeService.getAll().forEach(System.out::println);
        operationTypeService.getMachineToolsForOperationTypeId(1).forEach(System.out::println);

        ProductService productService = new ProductService(DBConnector.getInstance());
        productService.getAll().forEach(System.out::println);

        ProductRecipeService productReportService = new ProductRecipeService(DBConnector.getInstance());
        productReportService.getAll().forEach(System.out::println);

        System.out.println(machineToolService.getById(1).toPrettyJson());
        System.out.println(operationTypeService.getById(1).toPrettyJson());
        System.out.println(productService.getById(1).toPrettyJson());
        System.out.println(productReportService.getByIdProduct(200).toPrettyJson());

        DBConnector.getInstance().disconnect();
    }
}