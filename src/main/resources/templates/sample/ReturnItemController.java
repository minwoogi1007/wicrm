/*
 * 이 컨트롤러는 비활성화됨 - ExchangeController만 사용
 * 교환/반품 기능은 /exchange/* URL로 ExchangeController에서 처리
 */
/*
package com.wio.repairsystem.controller;

import com.wio.repairsystem.dto.ReturnItemDTO;
import com.wio.repairsystem.dto.ReturnItemSearchDTO;
import com.wio.repairsystem.model.ReturnStatus;
import com.wio.repairsystem.model.ReturnType;
import com.wio.repairsystem.service.ReturnItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/return-items")
@RequiredArgsConstructor
public class ReturnItemController {

    private final ReturnItemService returnItemService;

    @GetMapping
    public String listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "returnRequestDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Model model) {

        Page<ReturnItemDTO> returnItems = returnItemService.findAll(page, size, sortBy, sortDir);
        Map<ReturnStatus, Long> statusCounts = returnItemService.getStatusCounts();

        model.addAttribute("returnItems", returnItems);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", returnItems.getTotalPages());
        model.addAttribute("totalItems", returnItems.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("ASC") ? "DESC" : "ASC");
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());

        return "return-items/list";
    }

    @GetMapping("/search")
    public String search(ReturnItemSearchDTO searchDTO, Model model) {
        Page<ReturnItemDTO> returnItems = returnItemService.search(searchDTO);
        Map<ReturnStatus, Long> statusCounts = returnItemService.getStatusCounts();

        model.addAttribute("returnItems", returnItems);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("currentPage", searchDTO.getPage());
        model.addAttribute("totalPages", returnItems.getTotalPages());
        model.addAttribute("totalItems", returnItems.getTotalElements());
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());

        return "return-items/list";
    }

    @GetMapping("/{id}")
    public String viewPage(@PathVariable Long id, Model model) {
        ReturnItemDTO returnItem = returnItemService.findById(id);
        model.addAttribute("returnItem", returnItem);
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());
        return "return-items/view";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("returnItem", new ReturnItemDTO());
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());
        return "return-items/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ReturnItemDTO returnItem = returnItemService.findById(id);
        model.addAttribute("returnItem", returnItem);
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());
        return "return-items/form";
    }

    @PostMapping
    public String save(@ModelAttribute ReturnItemDTO returnItemDTO) {
        returnItemService.save(returnItemDTO);
        return "redirect:/return-items";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute ReturnItemDTO returnItemDTO) {
        returnItemService.update(id, returnItemDTO);
        return "redirect:/return-items/" + id;
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        returnItemService.delete(id);
        return "redirect:/return-items";
    }

    @GetMapping("/by-status/{status}")
    public String listByStatus(
            @PathVariable ReturnStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<ReturnItemDTO> returnItems = returnItemService.findByStatus(status, page, size);
        Map<ReturnStatus, Long> statusCounts = returnItemService.getStatusCounts();

        model.addAttribute("returnItems", returnItems);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", returnItems.getTotalPages());
        model.addAttribute("totalItems", returnItems.getTotalElements());
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());

        return "return-items/list";
    }

    // REST API Endpoints for AJAX calls
    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<ReturnItemDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam ReturnStatus status) {

        ReturnItemDTO updatedItem = returnItemService.updateStatus(id, status);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/api/unprocessed")
    @ResponseBody
    public ResponseEntity<Object> getUnprocessedItems() {
        return ResponseEntity.ok(returnItemService.findUnprocessed());
    }

    @GetMapping("/api/status-counts")
    @ResponseBody
    public ResponseEntity<Map<ReturnStatus, Long>> getStatusCounts() {
        return ResponseEntity.ok(returnItemService.getStatusCounts());
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<ReturnItemDTO> getReturnItem(@PathVariable Long id) {
        return ResponseEntity.ok(returnItemService.findById(id));
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<ReturnItemDTO> createReturnItem(@RequestBody ReturnItemDTO returnItemDTO) {
        ReturnItemDTO savedItem = returnItemService.save(returnItemDTO);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<ReturnItemDTO> updateReturnItem(
            @PathVariable Long id,
            @RequestBody ReturnItemDTO returnItemDTO) {

        ReturnItemDTO updatedItem = returnItemService.update(id, returnItemDTO);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteReturnItem(@PathVariable Long id) {
        returnItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 
*/ 