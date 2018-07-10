import { BaseEntity, User } from './../../shared';

export class Tag implements BaseEntity {
    constructor(
        public id?: number,
        public name?: string,
        public type?: string,
        public photos?: BaseEntity[],
        public user?: User,
        public parentTag?: BaseEntity,
    ) {
    }
}
