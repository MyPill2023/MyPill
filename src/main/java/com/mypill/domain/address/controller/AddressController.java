package com.mypill.domain.address.controller;

import com.mypill.domain.address.dto.request.AddressRequest;
import com.mypill.domain.address.dto.response.AddressResponse;
import com.mypill.domain.address.entity.Address;
import com.mypill.domain.address.service.AddressService;
import com.mypill.global.AppConfig;
import com.mypill.global.rq.Rq;
import com.mypill.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/buyer/myAddress")
@Tag(name = "AddressController", description = "배송지")
public class AddressController {

    private final AddressService addressService;
    private final Rq rq;

    @PreAuthorize("hasAuthority('BUYER')")
    @GetMapping("/create")
    @Operation(summary = "배송지 등록 페이지")
    public String createForm() {
        if (addressService.cannotCreate(rq.getMember().getId())) {
            return rq.historyBack("배송지는 최대 " + AppConfig.getMaxAddressCount() + "개 까지 등록 가능합니다.");
        }
        return "usr/address/create";
    }

    @PreAuthorize("hasAuthority('BUYER')")
    @PostMapping("/create")
    @Operation(summary = "배송지 등록")
    public String create(@Valid AddressRequest addressRequest) {
        RsData<Address> rsData = addressService.create(addressRequest);
        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }
        return rq.redirectWithMsg("/buyer/myAddress", rsData);
    }

    @PreAuthorize("hasAuthority('BUYER')")
    @GetMapping("/update/{addressId}")
    @Operation(summary = "배송지 수정 페이지")
    public String updateForm(@PathVariable Long addressId, Model model) {
        RsData<Address> rsData = addressService.get(rq.getMember(), addressId);
        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }
        model.addAttribute("address", AddressResponse.of(rsData.getData()));
        return "usr/address/update";
    }

    @PreAuthorize("hasAuthority('BUYER')")
    @PostMapping("/update/{addressId}")
    @Operation(summary = "배송지 수정")
    public String update(@PathVariable Long addressId, @Valid AddressRequest addressRequest) {
        RsData<Address> rsData = addressService.get(rq.getMember(), addressId);
        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }
        RsData<Address> updateRsData = addressService.update(rsData.getData(), addressRequest);
        return rq.redirectWithMsg("/buyer/myAddress", updateRsData);
    }

    @PreAuthorize("hasAuthority('BUYER')")
    @PostMapping("/delete/{addressId}")
    @Operation(summary = "배송지 삭제")
    public String delete(@PathVariable Long addressId) {
        RsData<Address> rsData = addressService.get(rq.getMember(), addressId);
        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }
        RsData<Address> deleteRsData = addressService.softDelete(rsData.getData());
        return rq.redirectWithMsg("/buyer/myAddress", deleteRsData);
    }

    @PreAuthorize("hasAuthority('BUYER')")
    @GetMapping("/getAddressDetails")
    @ResponseBody
    @Operation(summary = "배송지 세부사항 가져오기")
    public ResponseEntity<?> getAddressDetails(@RequestParam Long addressId) {
        Address address = addressService.findById(addressId).orElse(null);
        if (address == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("배송지를 찾을 수 없습니다.");
        }
        return ResponseEntity.ok().body(AddressResponse.of(address));
    }
}
