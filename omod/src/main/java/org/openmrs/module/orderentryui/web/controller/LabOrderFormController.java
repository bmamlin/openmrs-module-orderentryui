/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.orderentryui.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.CareSetting;
import org.openmrs.DrugOrder.DosingType;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.TestOrder;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.orderentryui.propertyeditor.CareSettingEditor;
import org.openmrs.module.orderentryui.propertyeditor.DosingTypeEditor;
import org.openmrs.module.orderentryui.propertyeditor.OrderFrequencyEditor;
import org.openmrs.validator.OrderValidator;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LabOrderFormController {

	@RequestMapping(value = "/module/orderentryui/labOrder", method = RequestMethod.GET)
	public void showForm() {
		
	}
	
	@ModelAttribute("labOrder")
	public Order getLabOrder(@RequestParam(value = "labOrderId", required = false) Integer labOrderId,
	        @RequestParam(value = "patientId", required = false) Integer patientId, ModelMap model) {
		Order labOrder = null;
		if (labOrderId != null)
			labOrder = Context.getOrderService().getOrder(labOrderId);
		else {
			labOrder = new TestOrder();
			labOrder.setCareSetting(Context.getOrderService().getCareSetting(2));
			if (patientId != null)
				labOrder.setPatient(Context.getPatientService().getPatient(patientId));
		}
		
		model.put("frequencies", Context.getOrderService().getOrderFrequencies(true));
		model.put("drugs", Context.getConceptService().getAllDrugs());
		
		return labOrder;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(OrderFrequency.class, new OrderFrequencyEditor());
		binder.registerCustomEditor(DosingType.class, new DosingTypeEditor());
		binder.registerCustomEditor(CareSetting.class, new CareSettingEditor());
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/module/orderentryui/labOrder")
	public String saveLabOrder(HttpServletRequest request, @ModelAttribute("labOrder") TestOrder labOrder, BindingResult result) {
		
		if (labOrder.getOrderer() == null) {
			labOrder.setOrderer(Context.getProviderService().getAllProviders().get(0));
			labOrder.setEncounter(Context.getEncounterService().getEncountersByPatient(labOrder.getPatient()).get(0));
			labOrder.setOrderType(Context.getOrderService().getOrderTypeByUuid("52a447d3-a64a-11e3-9aeb-50e549534c5e"));
		}
		
		new OrderValidator().validate(labOrder, result);
		if (!result.hasErrors()) {
			try {
				Context.getOrderService().saveOrder(labOrder, null);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "TestOrder.saved");
				
				return "redirect:" + "/patientDashboard.form?patientId=" + labOrder.getPatient().getPatientId();
			}
			catch (APIException e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "TestOrder.save.error");
			}
		}
		
		return null;
	}
}
