import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { HttpResponse } from '@angular/common/http';
import { Tag } from './tag.model';
import { TagService } from './tag.service';

@Injectable()
export class TagPopupService {
    private ngbModalRef: NgbModalRef;

    constructor(
        private modalService: NgbModal,
        private router: Router,
        private tagService: TagService

    ) {
        this.ngbModalRef = null;
    }

    open(component: Component, id?: number | any, parentTagId?: number): Promise<NgbModalRef> {
        return new Promise<NgbModalRef>((resolve, reject) => {
            const isOpen = this.ngbModalRef !== null;
            if (isOpen) {
                resolve(this.ngbModalRef);
            }

            const promises: any[] = [];
            if (id) {
                promises.push(this.tagService.find(id).toPromise());
            }
            if (parentTagId) {
                promises.push(this.tagService.find(parentTagId).toPromise());
            }

            if (promises.length > 0) {

                Promise.all(promises)
                    .then((responses: HttpResponse<any>[]) => {

                        let tag: Tag = undefined;
                        let parentTag: Tag = undefined;

                        if (id && parentTagId) {
                            tag = responses[0].body;
                            parentTag = responses[1].body;
                        } else if(id) {
                            tag = responses[0].body;
                        } else {
                            parentTag = responses[0].body;
                        }

                        this.ngbModalRef = this.tagModalRef(component, tag || new Tag(), parentTag);
                        resolve(this.ngbModalRef);
                    });

            } else {
                // setTimeout used as a workaround for getting ExpressionChangedAfterItHasBeenCheckedError
                setTimeout(() => {
                    this.ngbModalRef = this.tagModalRef(component, new Tag(), undefined);
                    resolve(this.ngbModalRef);
                }, 0);
            }
        });
    }

    tagModalRef(component: Component, tag: Tag, parentTag: Tag): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.tag = tag;
        modalRef.componentInstance.parentTag = parentTag;
        modalRef.result.then((result) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true, queryParamsHandling: 'merge' });
            this.ngbModalRef = null;
        }, (reason) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true, queryParamsHandling: 'merge' });
            this.ngbModalRef = null;
        });
        return modalRef;
    }
}
