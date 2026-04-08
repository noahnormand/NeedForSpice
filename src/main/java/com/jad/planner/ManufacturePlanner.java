package com.jad.planner;

import com.jad.dto.MachineScheduleDTO;
import com.jad.dto.ManufactureOrderDTO;
import com.jad.entity.*;
import com.jad.service.MachineToolService;
import com.jad.service.OperationTypeService;
import com.jad.service.ProductRecipeService;
import com.jad.service.ProductService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManufacturePlanner {
    private final ProductService productService;
    private final ProductRecipeService recipeService;
    private final OperationTypeService operationService;
    private final MachineToolService machineService;

    private final List<MachineScheduleDTO> finalSchedule;

    public ManufacturePlanner(ProductService productService, ProductRecipeService recipeService,
                              OperationTypeService operationService, MachineToolService machineService) {
        this.productService = productService;
        this.recipeService = recipeService;
        this.operationService = operationService;
        this.machineService = machineService;
        this.finalSchedule = new ArrayList<>();
    }

    public List<MachineScheduleDTO> generatePlan(int productId, double quantity) throws SQLException {
        this.finalSchedule.clear();
        planProduction(productId, quantity);
        return this.finalSchedule;
    }

    private void planProduction(int productId, double quantity) throws SQLException {
        Product product = productService.getById(productId);
        if (product == null || product.getIsAtomic()) {
            return;
        }

        ProductRecipe recipe = recipeService.getByProduct(product);
        if (recipe == null) return;

        OperationType operation = operationService.getById(recipe.getIdOperationType());
        double lossPercentage = (operation != null && operation.getLossOfQuantity() != null) ? operation.getLossOfQuantity() : 0.0;
        double grossQuantity = quantity * (1.0 + (lossPercentage / 100.0));

        for (RecipeLine line : recipe.getRecipeLines()) {
            double componentQuantity = grossQuantity * (line.getPercentage() / 100.0);
            planProduction(line.getIdComponent(), componentQuantity);
        }

        assignToMachine(recipe.getIdOperationType(), productId, quantity);
    }

    private void assignToMachine(int operationTypeId, int productId, double quantity) throws SQLException {
        List<MachineTool> availableMachines = operationService.getMachineToolsForOperationTypeId(operationTypeId);

        if (availableMachines.isEmpty()) {
            System.err.println("Alerte : Aucune machine trouvée pour l'opération " + operationTypeId);
            return;
        }

        MachineTool bestMachine = null;
        MachineScheduleDTO bestSchedule = null;
        int minWorkload = Integer.MAX_VALUE;

        for (MachineTool machine : availableMachines) {
            int machineId = machine.getId();
            MachineScheduleDTO currentSchedule = null;

            for (MachineScheduleDTO schedule : this.finalSchedule) {
                if (schedule.idMachineTool() == machineId) {
                    currentSchedule = schedule;
                    break;
                }
            }

            int workload = (currentSchedule == null) ? 0 : currentSchedule.orders().size();

            if (workload < minWorkload) {
                minWorkload = workload;
                bestMachine = machine;
                bestSchedule = currentSchedule;
            }
        }

        if (bestSchedule == null) {
            bestSchedule = new MachineScheduleDTO(bestMachine.getId(), new ArrayList<>());
            this.finalSchedule.add(bestSchedule);
        }

        double maxQty = bestMachine.getMaxQuantity();
        double remainingQty = quantity;

        while (remainingQty > 0) {
            double orderQty = Math.min(remainingQty, maxQty);

            int currentOrderNum = bestSchedule.orders().size();

            ManufactureOrderDTO order = new ManufactureOrderDTO(currentOrderNum, productId, orderQty);
            bestSchedule.orders().add(order);

            remainingQty -= orderQty;
        }
    }
}