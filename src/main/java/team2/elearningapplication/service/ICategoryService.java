package team2.elearningapplication.service;

import team2.elearningapplication.dto.common.ResponseCommon;
import team2.elearningapplication.dto.request.admin.category.AddCategoryRequest;
import team2.elearningapplication.dto.request.admin.category.DeleteCategoryRequest;
import team2.elearningapplication.dto.request.admin.category.UpdateCategoryRequest;
import team2.elearningapplication.dto.response.admin.category.AddCategoryResponse;
import team2.elearningapplication.dto.response.admin.category.DeleteCategoryResponse;
import team2.elearningapplication.dto.response.admin.category.FindAllCategoryResponse;
import team2.elearningapplication.dto.response.admin.category.UpdateCategoryResponse;

public interface ICategoryService {
    ResponseCommon<AddCategoryResponse> addCategory(AddCategoryRequest addCategoryRequest);

    ResponseCommon<UpdateCategoryResponse> updateCategory(UpdateCategoryRequest addCategoryRequest);

    ResponseCommon<DeleteCategoryResponse> deleteCategory(DeleteCategoryRequest deleteCategoryRequest);

    ResponseCommon<FindAllCategoryResponse> findAllCategory();
}
