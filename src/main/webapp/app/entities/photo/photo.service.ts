import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { SERVER_API_URL } from '../../app.constants';

import { JhiDateUtils } from 'ng-jhipster';

import { Photo } from './photo.model';
import { createRequestOption } from '../../shared';

export type EntityResponseType = HttpResponse<Photo>;

@Injectable()
export class PhotoService {

    private resourceUrl =  SERVER_API_URL + 'api/photos';

    constructor(private http: HttpClient, private dateUtils: JhiDateUtils) { }

    save(formData: FormData, create: boolean): Observable<any> {

        if (create) {
            return this.http.post(this.resourceUrl + "/create", formData);
        } else {
            return this.http.post(this.resourceUrl + "/update", formData);
        }

    }

    find(id: number): Observable<EntityResponseType> {
        return this.http.get<Photo>(`${this.resourceUrl}/${id}`, { observe: 'response'})
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    query(req?: any): Observable<HttpResponse<Photo[]>> {
        const options = createRequestOption(req);
        return this.http.get<Photo[]>(this.resourceUrl, { params: options, observe: 'response' })
            .map((res: HttpResponse<Photo[]>) => this.convertArrayResponse(res));
    }

    queryByTag(tagId?: number, req?: any): Observable<HttpResponse<Photo[]>> {
        const options = createRequestOption(req);
        return this.http.get<Photo[]>(`${this.resourceUrl}/tag/${tagId}`, { params: options, observe: 'response' })
            .map((res: HttpResponse<Photo[]>) => this.convertArrayResponse(res));
    }

    getPhotoImage(photoId: number): Observable<string> {
        return this.http.get(`${this.resourceUrl}/${photoId}/image`, { responseType: 'text' });
    }

    delete(id: number): Observable<HttpResponse<any>> {
        return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }

    private convertResponse(res: EntityResponseType): EntityResponseType {
        const body: Photo = this.convertItemFromServer(res.body);
        return res.clone({body});
    }

    private convertArrayResponse(res: HttpResponse<Photo[]>): HttpResponse<Photo[]> {
        const jsonResponse: Photo[] = res.body;
        const body: Photo[] = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            body.push(this.convertItemFromServer(jsonResponse[i]));
        }
        return res.clone({body});
    }

    /**
     * Convert a returned JSON object to Photo.
     */
    private convertItemFromServer(photo: Photo): Photo {
        const copy: Photo = Object.assign({}, photo);
        copy.dateCreated = this.dateUtils
            .convertDateTimeFromServer(photo.dateCreated);
        return copy;
    }

    /**
     * Convert a Photo to a JSON which can be sent to the server.
     */
    private convert(photo: Photo): Photo {
        const copy: Photo = Object.assign({}, photo);

        copy.dateCreated = this.dateUtils.toDate(photo.dateCreated);
        return copy;
    }
}
