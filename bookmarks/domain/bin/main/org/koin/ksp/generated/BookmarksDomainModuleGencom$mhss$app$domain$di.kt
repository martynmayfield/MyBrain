package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_domain_di_BookmarksDomainModule : Module get() = module {
	single() { _ -> com.mhss.app.domain.use_case.UpdateBookmarkUseCase(bookmarkRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.AddBookmarkUseCase(bookmarkRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.SearchBookmarksUseCase(bookmarksRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetAllBookmarksUseCase(bookmarksRepository=get(),defaultDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("defaultDispatcher"))) } 
	single() { _ -> com.mhss.app.domain.use_case.DeleteBookmarkUseCase(bookmarkRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetBookmarkUseCase(bookmarkRepository=get()) } 
}
public val com.mhss.app.domain.di.BookmarksDomainModule.module : org.koin.core.module.Module get() = com_mhss_app_domain_di_BookmarksDomainModule
