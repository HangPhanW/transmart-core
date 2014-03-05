/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/

/*
 * $Id: CustomFilterItem.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package org.transmart.searchapp

class CustomFilterItem {
    Long id
    String uniqueId
    String bioDataType
    static belongsTo = [customFilter: CustomFilter]
    static mapping = {
        table 'SEARCH_CUSTOM_FILTER_ITEM'
        version false
        id generator: 'sequence', params: [sequence: 'SEQ_SEARCH_DATA_ID']
        columns {
            id column: 'SEARCH_CUSTOM_FILTER_ITEM_ID'
            customFilter column: 'SEARCH_CUSTOM_FILTER_ID'
            uniqueId column: 'UNIQUE_ID'
            bioDataType column: 'BIO_DATA_TYPE'
        }
    }
    static constraints = {
        bioDataType(maxSize: 100)
    }
}
