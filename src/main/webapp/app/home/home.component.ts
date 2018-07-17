import { Component, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager, JhiParseLinks } from 'ng-jhipster';

import { Account, LoginModalService, Principal, ITEMS_PER_PAGE } from '../shared';
import { TagService } from '../entities/tag/tag.service';
import { Tag } from '../entities/tag/tag.model';
import { HttpResponse } from '@angular/common/http';
import { PhotoService } from '../entities/photo/photo.service';
import { Photo } from '../entities/photo/photo.model';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: [
        'home.scss'
    ]

})
export class HomeComponent implements OnInit {

    account: Account;
    modalRef: NgbModalRef;
    tags: Tag[];
    rootTag: Tag = {
        id: 0,
        name: 'ROOT'
    };
    history: Tag[];
    photos: Photo[];
    photosSelectedMap: { [photoId: string]: Photo };

    routeData: any;
    links: any;
    totalItems: any;
    queryCount: any;
    itemsPerPage: any;
    page: any;
    predicate: any;
    previousPage: any;
    reverse: any;

    constructor(
        private principal: Principal,
        private loginModalService: LoginModalService,
        private tagService: TagService,
        private photoService: PhotoService,
        private eventManager: JhiEventManager,
        private activatedRoute: ActivatedRoute,
        private parseLinks: JhiParseLinks,
        private router: Router
    ) {
    }

    ngOnInit() {
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.routeData = this.activatedRoute.data.subscribe((data) => {
            this.page = data.pagingParams.page;
            this.previousPage = data.pagingParams.page;
            this.reverse = data.pagingParams.ascending;
            this.predicate = data.pagingParams.predicate;
        });
        this.history = [];
        this.principal.identity().then((account) => {
            this.account = account;
            if (this.account !== null) {
                this.selectTag(this.rootTag);
            }
        });
        this.registerAuthenticationSuccess();
        this.registerChangeInPhotos();
        this.registerChangeInTags();
    }

    registerChangeInPhotos() {
        this.eventManager.subscribe('photoListModification', (response) => this.loadPhotos(this.getTagSelected()));
    }

    registerChangeInTags() {
        this.eventManager.subscribe('tagListModification', (response: any) => {

            if (response.content) {
                this.selectTag(this.getTagSelected());
            } else {
                const parentLastTag = this.history[this.history.length - 2];
                this.selectTag(parentLastTag);
            }

        });
    }

    registerAuthenticationSuccess() {
        this.eventManager.subscribe('authenticationSuccess', (message) => {
            this.principal.identity().then((account) => {
                this.account = account;
                if (this.account !== null) {
                    this.selectTag(this.rootTag);
                }
            });
        });
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }

    clickOnTag(tag) {
        this.selectTag(tag);
    }

    selectTag(tag: Tag) {

        this.updateHistory(tag);

        this.tagService.findSonsOfTag(tag.id).subscribe((responseTags: HttpResponse<Tag[]>) => {
            this.tags = responseTags.body;
        });

        this.loadPhotos(tag);

    }

    updateHistory(tag: Tag) {
        const i = this.history.indexOf(tag);
        if (i !== -1) {
            this.history.splice(i, this.history.length - i);
        }
        this.history.push(tag);
    }

    loadPhotos(tag: Tag) {

        const params = {
            page: this.page - 1,
            size: this.itemsPerPage,
            sort: this.sort()
        };

        this.photoService.queryByTag(tag.id, params)
            .subscribe((res: HttpResponse<Photo[]>) => this.onSuccess(res.body, res.headers));

    }

    onSuccess(data, headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.photos = data;
    }

    sort() {
        const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    loadPage(page: number) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        this.router.navigate(['/photo'], {queryParams:
            {
                page: this.page,
                size: this.itemsPerPage,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            }
        });
        this.loadPhotos(this.getTagSelected());
    }

    clear() {
        this.page = 0;
        this.router.navigate(['/photo', {
            page: this.page,
            sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
        }]);
        this.loadPhotos(this.getTagSelected());
    }

    getTagSelected() {
        return this.history[this.history.length - 1];
    }

    onClickPhoto(photo: Photo) {

    }

    onSelectPhoto(photo: Photo) {
        this.photosSelectedMap = this.createIfNotExist(this.photosSelectedMap);
        if (this.photosSelectedMap.hasOwnProperty(photo.id)) {
            delete this.photosSelectedMap[photo.id];
        } else {
            this.photosSelectedMap[photo.id] = photo;
        }
    }

    isPhotoSelected(photo: Photo) {
        this.photosSelectedMap = this.createIfNotExist(this.photosSelectedMap);
        return this.photosSelectedMap.hasOwnProperty(photo.id);
    }

    createIfNotExist(map: any): any {
        return (!map) ? {} : map;
    }

}
