/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.api.lite.content;

/**
 * Supplemental object for tracking transactions where sub-nodes are included
 * 
 * @param from
 *          - Original path of object
 * @param to
 *          - Final path of object (can be null if transaction is delete)
 */
public class ActionRecord {
  private String from;
  private String to;

  public ActionRecord(String newFrom, String newTo) {
    from = newFrom;
    to = newTo;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }
}